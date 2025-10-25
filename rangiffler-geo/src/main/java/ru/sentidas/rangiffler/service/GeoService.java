package ru.sentidas.rangiffler.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class GeoService {

    private static final Logger LOG = LoggerFactory.getLogger(GeoService.class);

    private final CountryRepository countryRepository;
    private final StatisticsRepository statisticsRepository;
    private final GrpcUserdataClient grpcUserdataClient;

    public GeoService(CountryRepository countryRepository,
                      StatisticsRepository statisticsRepository,
                      GrpcUserdataClient grpcUserdataClient) {
        this.countryRepository = countryRepository;
        this.statisticsRepository = statisticsRepository;
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = GeoCacheConfig.CACHE_COUNTRIES,
            key = "'all'",
            unless = "#result == null || #result.isEmpty()")
    public List<Country> allCountries() {
        return Country.fromEntity(countryRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<Country> getByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        final List<CountryEntity> rows = countryRepository.findAllByCodeIn(codes);
        final Map<String, Country> byCode = new HashMap<>(rows.size());
        for (CountryEntity e : rows) {
            Country c = Country.fromEntity(e);
            byCode.put(c.code(), c);
        }

        final List<Country> result = new ArrayList<>(codes.size());
        for (String code : codes) {
            Country c = byCode.get(code);
            if (c != null) {
                result.add(c);
            }
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
        grpcUserdataClient.usernameById(userId);
        // множество userIds: сам пользователь + (опционально) друзья
        final Set<UUID> userIds = new HashSet<>();
        userIds.add(userId);

        if (withFriends) {
            userIds.addAll(grpcUserdataClient.friendIdsAll(userId));
        }

        List<StatisticsRepository.CountryStatRow> rows = statisticsRepository.aggregateByUserIds(userIds);
        if (rows.isEmpty()) {
            return List.of();
        }

        final List<UUID> countryIds = rows.stream().map(StatisticsRepository.CountryStatRow::getCountryId).toList();
        final Map<UUID, CountryEntity> countries = countryRepository.findAllById(countryIds)
                .stream().collect(java.util.stream.Collectors.toMap(CountryEntity::getId, c -> c));

        return rows.stream().map(r -> new Stat(
                r.getTotal(),
                Country.fromEntity(countries.get(r.getCountryId()))
        )).toList();
    }

    @Transactional
    @KafkaListener(topics = "rangiffler_photo")
    public void listener(@Payload PhotoStatEvent photoStatEvent, ConsumerRecord<String, PhotoStatEvent> cr) {
        final Optional<CountryEntity> maybeCountry = countryRepository.findByCode(photoStatEvent.countryCode());
        if (maybeCountry.isEmpty()) { // ADD
            LOG.warn("Skip event: unknown country code={}, userId={}, partition={}, offset={}",
                    photoStatEvent.countryCode(), photoStatEvent.userId(), cr.partition(), cr.offset());
            return;
        }
        final CountryEntity country = maybeCountry.get();

        final UUID userId = photoStatEvent.userId();
        final UUID countryId = country.getId();
        final int delta = photoStatEvent.delta(); // ожидаем +1 при создании фото и -1 при удалении


        if (delta == 0) {
            LOG.warn("Skip event: delta=0 (no-op) userId={}, countryId={}, partition={}, offset={}",
                    userId, countryId, cr.partition(), cr.offset());
            return;
        }

        // Инкремент: upsert одной командой (новая строка или +1 к существующей)
        if (delta > 0) {
            int affected = statisticsRepository.upsertIncrement(userId, countryId);
            LOG.debug("Stat +1 (upsert): user={}, country={}, affected={}", userId, countryId, affected);
            return;
        }

        if (delta < 0) {
            // Декремент: в два шага — сначала уменьшаем (>1 → -1), иначе удаляем строку (1 → 0)
            final int updated = statisticsRepository.decreaseCountIfGreaterThanOne(userId, countryId);
            if (updated == 0) {
                final int deleted = statisticsRepository.deleteIfCountEqualsOne(userId, countryId);

                if (deleted == 1) {
                    LOG.debug("Stat -1: user={}, country={}, case=count=1 -> row deleted", userId, countryId);
                } else {
                    LOG.warn("Stat -1 ignored: no row for user={}, country={}", userId, countryId);
                }
            } else {
                // Было > 1, теперь стало >= 1 (2->1 или 3->2 и т.д.). Удаления не требуется.
                LOG.debug("Stat -1: user={}, country={}, decreased (>1 -> -1), updated={}", userId, countryId, updated);
            }
        }
    }
}
