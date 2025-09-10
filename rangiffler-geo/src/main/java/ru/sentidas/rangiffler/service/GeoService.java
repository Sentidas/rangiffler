package ru.sentidas.rangiffler.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.entity.CountryEntity;
import ru.sentidas.rangiffler.entity.StatisticEntity;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.PhotoStatEvent;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.repository.CountryRepository;
import ru.sentidas.rangiffler.repository.StatisticsRepository;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class GeoService {

    private static final Logger LOG = LoggerFactory.getLogger(GeoService.class);

    private final CountryRepository countryRepository;
    private final StatisticsRepository statisticsRepository;
    private final GrpcUserdataClient grpcUserdataClient;

    public GeoService(CountryRepository countryRepository, StatisticsRepository statisticsRepository, GrpcUserdataClient grpcUserdataClient) {
        this.countryRepository = countryRepository;
        this.statisticsRepository = statisticsRepository;
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @Transactional(readOnly = true)
    public List<Country> allCountries() {
        return Country.fromEntity(countryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Country getByCode(String code) {
        CountryEntity country = countryRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Country not found: " + code));
        if (country == null) {
            throw new jakarta.persistence.EntityNotFoundException("Country not found: " + code);
        }
        return Country.fromEntity(country);
    }

    @Transactional(readOnly = true)
    public List<Stat> statByUserId(UUID userId, boolean withFriends) {
        // формируем множество userIds: сам пользователь + (опционально) друзья
        java.util.Set<UUID> userIds = new java.util.HashSet<>();
        userIds.add(userId);

        if (withFriends) {
            userIds.addAll(grpcUserdataClient.friendIdsAll(userId));
        }

        if (userIds.isEmpty()) return List.of();

        // 1 запрос к БД: агрегаты по странам
        var rows = statisticsRepository.aggregateByUserIds(userIds);
        if (rows.isEmpty()) return List.of();

        // подтягиваем страны одним запросом
        var countryIds = rows.stream().map(StatisticsRepository.CountryStatRow::getCountryId).toList();
        var countries = countryRepository.findAllById(countryIds)
                .stream().collect(java.util.stream.Collectors.toMap(CountryEntity::getId, c -> c));

        // маппим в твой модельный Stat (count + Country)
        return rows.stream().map(r -> new Stat(
                r.getTotal(),
                Country.fromEntity(countries.get(r.getCountryId()))
        )).toList();
    }


    @Transactional
    @KafkaListener(topics = "rangiffler_photo", groupId = "geo")
    public void listener(@Payload PhotoStatEvent photoStatEvent, ConsumerRecord<String, PhotoStatEvent> cr) {
        CountryEntity country = countryRepository.findByCode(photoStatEvent.countryCode())
                .orElseThrow(() -> new NotFoundException("Country not found: " + photoStatEvent.countryCode()));


        // 2) Достаём нужные поля из события/страны для удобства
        UUID userId = photoStatEvent.userId();
        UUID countryId = country.getId();
        int delta = photoStatEvent.delta(); // ожидаем +1 при создании фото и -1 при удалении

        // 3) Ищем текущую запись статистики по связке (user, country)
        statisticsRepository.findByUserIdAndCountryId(userId, countryId)
                .ifPresentOrElse(
                        s -> {
                            // 3a) Запись есть — пересчитываем счётчик
                            int newCount = s.getCount() + delta;

                            if (newCount <= 0) {
                                // Если после применения дельты счётчик <= 0 — запись больше не нужна, удаляем
                                statisticsRepository.delete(s);
                                LOG.info("Statistic removed: user={}, country={}", userId, countryId);
                            } else {
                                // Иначе просто обновляем значение и сохраняем
                                s.setCount(newCount);
                                statisticsRepository.save(s);
                                LOG.info("Statistic updated: user={}, country={}, count={}", userId, countryId, newCount);
                            }
                        },
                        () -> {
                            // 3b) Записи нет
                            if (delta > 0) {
                                // Пришла положительная дельта — создаём новую запись со стартовым значением = delta
                                StatisticEntity s = new StatisticEntity();
                                s.setUserId(userId);
                                s.setCountryId(countryId);
                                s.setCount(delta);
                                statisticsRepository.save(s);
                                LOG.info("Statistic created: user={}, country={}, count={}", userId, countryId, delta);
                            } else {
                                // Пришла отрицательная дельта, а записи нет — ничего не делаем (идемпотентность)
                                LOG.info("Ignore negative delta for missing stat: user={}, country={}", userId, countryId);
                            }
                        }
                );
    }
}
