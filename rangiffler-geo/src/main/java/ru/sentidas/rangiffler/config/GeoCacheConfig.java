package ru.sentidas.rangiffler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.sentidas.rangiffler.model.Country;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableCaching
public class GeoCacheConfig extends CachingConfigurerSupport {

    private static final Logger log = LoggerFactory.getLogger(GeoCacheConfig.class);

    public static final String CACHE_COUNTRIES = "countries";
    public static final String CACHE_COUNTRY_BY_CODE = "countryByCode";

    /**
     * ЕДИНСТВЕННЫЙ CacheManager: всегда RedisCacheManager.
     * Доступность Redis во время работы решается timeout'ом и CacheErrorHandler'ом (мягко идём в БД).
     */
    @Bean
    @Primary
    public CacheManager cacheManager(ObjectMapper om, RedisConnectionFactory redisCf) {
        // Сериализация/TTL/префикс — как у тебя
        om.findAndRegisterModules();

        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        var countryType   = om.getTypeFactory().constructType(Country.class);
        var countriesType = om.getTypeFactory().constructCollectionType(List.class, Country.class);

        var countrySer   = new Jackson2JsonRedisSerializer<>(om, countryType);
        var countriesSer = new Jackson2JsonRedisSerializer<>(om, countriesType);

        var base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keyPair)
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(om)))
                .disableCachingNullValues()
                .prefixCacheNameWith("rangiffler-geo:")
                .entryTtl(Duration.ofMinutes(5));

        Map<String, RedisCacheConfiguration> caches = new HashMap<>();
        caches.put(CACHE_COUNTRIES,
                base.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(countriesSer))
                        .entryTtl(Duration.ofMinutes(5)));
        caches.put(CACHE_COUNTRY_BY_CODE,
                base.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(countrySer))
                        .entryTtl(Duration.ofMinutes(10)));

        log.info("[GeoCache] CacheManager = RedisCacheManager (без фолбэков).");
        return RedisCacheManager.builder(redisCf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(caches)
                .enableStatistics()
                .build();
    }

    /**
     * Мягкий обработчик: ошибки Redis НЕ бросаем → метод идёт в БД.
     * Так мы переживаем таймауты/обрывы без падения.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            private final Logger clog = LoggerFactory.getLogger("CacheErrorHandler");
            private String name(org.springframework.cache.Cache c){ return c != null ? c.getName() : "<null>"; }

            @Override public void handleCacheGetError(RuntimeException e, org.springframework.cache.Cache c, Object k) {
                clog.debug("Cache GET error on {} key={}: {}", name(c), k, e.toString());
            }
            @Override public void handleCachePutError(RuntimeException e, org.springframework.cache.Cache c, Object k, Object v) {
                clog.debug("Cache PUT error on {} key={}: {}", name(c), k, e.toString());
            }
            @Override public void handleCacheEvictError(RuntimeException e, org.springframework.cache.Cache c, Object k) {
                clog.debug("Cache EVICT error on {} key={}: {}", name(c), k, e.toString());
            }
            @Override public void handleCacheClearError(RuntimeException e, org.springframework.cache.Cache c) {
                clog.debug("Cache CLEAR error on {}: {}", name(c), e.toString());
            }
        };
    }
}
