package ru.sentidas.rangiffler.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.sentidas.rangiffler.ActivityEvent;
import ru.sentidas.rangiffler.EventType;
import ru.sentidas.rangiffler.config.ActivityPublisher;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityPublisher publisher;

    public void publishPhotoEvent(EventType type,
                                  UUID initiatorUserId,
                                  UUID photoId,
                                  UUID ownerUserId,
                                  String countryCode) {

        Map<String, Object> payload = new HashMap<>();
        if (countryCode != null && !countryCode.isBlank()) {
            payload.put("countryCode", countryCode);
        }

        ActivityEvent event = new ActivityEvent(
                UUID.randomUUID(),     // eventId
                type,                  // тип события
                Instant.now(),
                initiatorUserId,       // actor (кто совершил действие)
                photoId,
                ownerUserId,           // targetUserId (владелец фото), тут совпадает с actor
                "photo",               // sourceService
                payload
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.publish(event);
                }
            });
        } else {
            // Вызов вне транзакции — публикуем сразу
            publisher.publish(event);
        }
    }
}
