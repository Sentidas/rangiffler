package ru.sentidas.rangiffler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class GeoCacheConfig {

    public static final String CACHE_COUNTRIES = "countries";
    public static final String CACHE_COUNTRY_BY_CODE = "countryByCode";

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory cf, ObjectMapper om) {
        om.findAndRegisterModules();

        // key serializer
        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        // value serializers (типизированные)
        var countryType = om.getTypeFactory().constructType(Country.class);
        var countriesType = om.getTypeFactory().constructCollectionType(List.class, Country.class);

        var countrySer = new Jackson2JsonRedisSerializer<>(om, countryType);
        var countriesSer = new Jackson2JsonRedisSerializer<>(om, countriesType);

        // базовая конфигурация
        var base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(keyPair)
                // дефолт для прочих кэшей (если вдруг появятся) — generic json
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(om)))
                .disableCachingNullValues()
                .prefixCacheNameWith("rangiffler-geo:")  // новый префикс
                .entryTtl(Duration.ofMinutes(5));

        Map<String, RedisCacheConfiguration> caches = new HashMap<>();
        caches.put(CACHE_COUNTRIES,
                base.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(countriesSer))
                        .entryTtl(Duration.ofMinutes(5)));

        caches.put(CACHE_COUNTRY_BY_CODE,
                base.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(countrySer))
                        .entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(cf)
                .cacheDefaults(base)
                .withInitialCacheConfigurations(caches)
                .enableStatistics()       // ← ВКЛЮЧАЕМ СТАТИСТИКУ
                .build();
    }
}
