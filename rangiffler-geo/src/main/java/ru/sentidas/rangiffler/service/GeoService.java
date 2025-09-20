package ru.sentidas.rangiffler.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.config.GeoCacheConfig;
import ru.sentidas.rangiffler.entity.CountryEntity;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.PhotoStatEvent;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.repository.CountryRepository;
import ru.sentidas.rangiffler.repository.StatisticsRepository;

import java.util.*;

@Component
@Transactional
public class GeoService {

    private static final Logger LOG = LoggerFactory.getLogger(GeoService.class);

    private final CountryRepository countryRepository;
    private final StatisticsRepository statisticsRepository;
    private final GrpcUserdataClient grpcUserdataClient;
    private final CacheManager cacheManager;

    public GeoService(CountryRepository countryRepository, StatisticsRepository statisticsRepository, GrpcUserdataClient grpcUserdataClient, CacheManager cacheManager) {
        this.countryRepository = countryRepository;
        this.statisticsRepository = statisticsRepository;
        this.grpcUserdataClient = grpcUserdataClient;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GeoCacheConfig.CACHE_COUNTRIES, unless = "#result == null || #result.isEmpty()")
    public List<Country> allCountries() {
        return Country.fromEntity(countryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<Country> getByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) return List.of();
        List<CountryEntity> rows = countryRepository.findAllByCodeIn(codes);
        Map<String, Country> byCode = new HashMap<>(rows.size());
        for (CountryEntity e : rows) {
            Country c = Country.fromEntity(e);
            byCode.put(c.code(), c);
        }
        List<Country> result = new ArrayList<>(codes.size());
        for (String code : codes) {
            Country c = byCode.get(code);
            if (c != null) result.add(c);
        }
        return result;
    }


    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GeoCacheConfig.CACHE_COUNTRY_BY_CODE,
            key = "T(org.springframework.util.StringUtils).hasText(#code) ? #code.toLowerCase() : #code",
            unless = "#result == null")
    public Country getByCode(String code) {
        CountryEntity country = countryRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("Country not found: " + code));
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

        if (delta > 0) {
            // === ВЕТКА «ПЛЮС» (+1) ===
            // Атомарно: INSERT (user,country,count=1) ИЛИ, если строка уже есть, UPDATE count = count + 1
            // Примеры:
            //   - строки нет  -> создастся (count станет 1) -> affected = 1
            //   - count = 5   -> станет 6                   -> affected = 2 (семантика MySQL для upsert)
            int affected = statisticsRepository.upsertIncrement(userId, countryId);
            LOG.info("Stat +1 (upsert): user={}, country={}, affected={}", userId, countryId, affected);
            return;
        }

        if (delta < 0) {
            // === ВЕТКА «МИНУС» (-1) ===
            // Шаг 1. Пытаемся уменьшить count на 1, ТОЛЬКО если он > 1 (один запрос).
            //
            // Примеры:
            //   A) count = 3  -> UPDATE сработает, станет 2, updated = 1 (на этом всё, удалять не надо)
            //   B) count = 2  -> UPDATE сработает, станет 1, updated = 1 (на этом всё, удалять не надо)
            //   C) count = 1  -> UPDATE не выполнится (условие count > 1 ложное), updated = 0
            //   D) строки нет -> UPDATE не выполнится, updated = 0
            int updated = statisticsRepository.decreaseCountIfGreaterThanOne(userId, countryId);

            if (updated == 0) {
                // Шаг 2. Условие: либо строки не было, либо count был ровно 1.
                // Пытаемся удалить строку, если count = 1 (представляем «1 → 0» именно как удаление строки).
                //
                // Примеры:
                //   C) count = 1  -> DELETE выполнится, deleted = 1 (строка удалена)
                //   D) строки нет -> DELETE ничего не сделает, deleted = 0 (идемпотентно игнорируем)
                int deleted = statisticsRepository.deleteIfCountEqualsOne(userId, countryId);

                if (deleted == 1) {
                    LOG.info("Stat -1: user={}, country={}, case=count=1 -> row deleted", userId, countryId);
                } else {
                    LOG.info("Stat -1 ignored: no row for user={}, country={}", userId, countryId);
                }
            } else {
                // Было > 1, теперь стало >= 1 (2->1 или 3->2 и т.д.). Удаления не требуется.
                LOG.info("Stat -1: user={}, country={}, decreased (>1 -> -1), updated={}", userId, countryId, updated);
            }
        }
    }
//        // 3) Ищем текущую запись статистики по связке (user, country)
//        statisticsRepository.findByUserIdAndCountryId(userId, countryId)
//                .ifPresentOrElse(
//                        s -> {
//                            // 3a) Запись есть — пересчитываем счётчик
//                            int newCount = s.getCount() + delta;
//
//                            if (newCount <= 0) {
//                                // Если после применения дельты счётчик <= 0 — запись больше не нужна, удаляем
//                                statisticsRepository.delete(s);
//                                LOG.info("Statistic removed: user={}, country={}", userId, countryId);
//                            } else {
//                                // Иначе просто обновляем значение и сохраняем
//                                s.setCount(newCount);
//                                statisticsRepository.save(s);
//                                LOG.info("Statistic updated: user={}, country={}, count={}", userId, countryId, newCount);
//                            }
//                        },
//                        () -> {
//                            // 3b) Записи нет
//                            if (delta > 0) {
//                                // Пришла положительная дельта — создаём новую запись со стартовым значением = delta
//                                StatisticEntity s = new StatisticEntity();
//                                s.setUserId(userId);
//                                s.setCountryId(countryId);
//                                s.setCount(delta);
//                                statisticsRepository.save(s);
//                                LOG.info("Statistic created: user={}, country={}, count={}", userId, countryId, delta);
//                            } else {
//                                // Пришла отрицательная дельта, а записи нет — ничего не делаем (идемпотентность)
//                                LOG.info("Ignore negative delta for missing stat: user={}, country={}", userId, countryId);
//                            }
//                        }
//                );
}
