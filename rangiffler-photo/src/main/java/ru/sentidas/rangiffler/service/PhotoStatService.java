package ru.sentidas.rangiffler.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.sentidas.rangiffler.model.PhotoStatEvent;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotoStatService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoStatService.class);

    private final KafkaTemplate<String, PhotoStatEvent> kafkaTemplate;

    /**
     * Отправка изменений статистики в топик "rangiffler_photo" после коммита
     */
    public void sendDeltaAfterCommit(UUID userId, String countryCode, int delta) {
        if (countryCode == null || countryCode.isBlank()) {
            return; // Нечего слать
        }

        PhotoStatEvent event = new PhotoStatEvent(userId, countryCode, delta);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    kafkaTemplate.send("rangiffler_photo", event);
                    LOG.debug("Kafka topic [rangiffler_photo] sent message: {}", event);
                }
            });
        } else {
            kafkaTemplate.send("rangiffler_photo", event);
            LOG.debug("Kafka topic [rangiffler_photo] sent message: {}", event);
        }
    }
}
