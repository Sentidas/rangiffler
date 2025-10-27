package ru.sentidas.rangiffler.media;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sentidas.rangiffler.ImageFormatValidator;
import ru.sentidas.rangiffler.MediaProperties;

/**
 * Валидатор форматов изображений для проверки изображений, загруженных с фронта
 */
@Configuration
@EnableConfigurationProperties
public class MediaConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.media")
    public MediaProperties mediaProperties() { return new MediaProperties(); }

    @Bean
    public ImageFormatValidator imageFormatValidator(MediaProperties props) {
        return new ImageFormatValidator(props.getAllowedMime());
    }
}
