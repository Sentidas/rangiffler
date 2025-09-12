package ru.sentidas.rangiffler.config;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import ru.sentidas.rangiffler.model.UserEvent;


@Configuration
public class RangifflerUserdataConsumerConfiguration {

    private final KafkaProperties kafkaProperties;

    @Autowired
    public RangifflerUserdataConsumerConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, UserEvent> consumerFactory(SslBundles sslBundles) {
        final JsonDeserializer<UserEvent> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(
                kafkaProperties.buildConsumerProperties(sslBundles),
                new StringDeserializer(),
                jsonDeserializer
        );
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> kafkaListenerContainerFactory(SslBundles sslBundles) {
        ConcurrentKafkaListenerContainerFactory<String, UserEvent> concurrentKafkaListenerContainerFactory
                = new ConcurrentKafkaListenerContainerFactory<>();
        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory(sslBundles));
        return concurrentKafkaListenerContainerFactory;
    }
}
