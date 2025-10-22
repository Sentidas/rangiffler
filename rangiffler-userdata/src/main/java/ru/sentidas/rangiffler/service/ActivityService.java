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

    /**
     * Публикует событие дружбы после коммита транзакции
     *
     * @param type          тип события
     * @param initiatorId   пользователь-инициатор
     * @param targetUserId  второй участник (может быть null)
     * @param countryCode   код страны (может быть null)
     */
    public void publishFriendEvent(EventType type,
                                   UUID initiatorId,
                                   UUID targetUserId,
                                   String countryCode) {

        final Map<String, Object> payload = new HashMap<>();
        if (countryCode != null && !countryCode.isBlank()) {
            payload.put("countryCode", countryCode);
        }

        final ActivityEvent evt = new ActivityEvent(
                UUID.randomUUID(),
                type,
                Instant.now(),
                initiatorId,
                null,
                targetUserId,
                "userdata",
                payload
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    publisher.publish(evt);
                }
            });
        } else {
            // если вызвали вне транзакции — публикуем сразу
            publisher.publish(evt);
        }
    }
}
