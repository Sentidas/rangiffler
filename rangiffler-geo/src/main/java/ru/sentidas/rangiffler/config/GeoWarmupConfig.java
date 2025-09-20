package ru.sentidas.rangiffler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sentidas.rangiffler.service.GeoService;

@Configuration
public class GeoWarmupConfig {
    private static final Logger log = LoggerFactory.getLogger(GeoWarmupConfig.class);

    /**
     * Греем кэш стран на старте.
     * Важно: вызываем через ВНЕДРЁННЫЙ бин geoService (а не self-call),
     * чтобы сработал @Cacheable-прокси.
     */
    @Bean
    ApplicationRunner warmupCountries(GeoService geoService) {
        return args -> {
            try {
                var list = geoService.allCountries();
                log.info("Warmup: countries loaded into cache, size={}", (list != null ? list.size() : 0));
            } catch (Exception e) {
                log.warn("Warmup: countries preload failed (will be loaded on-demand)", e);
            }
        };
    }
}
