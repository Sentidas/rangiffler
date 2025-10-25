package ru.sentidas.rangiffler;

import java.util.UUID;

public final class MessageKeyStrategy {
    private MessageKeyStrategy() {}

    /**
     * Стратегия A (простая и надёжная для аудита):
     *  - всегда key = userId.toString()
     *  Плюсы: порядок по пользователю сохраняется,
     *  Минусы: потенциальный "горячий" ключ, решается количеством партиций.
     */
    public static String auditKeyByUser(UUID userId) {
        return userId == null ? "unknown" : userId.toString();
    }

    /**
     * Стратегия B (если нужен лучший баланс по партициям):
     *  - PHOTO_* / LIKE_* -> key = photoId
     *  - остальное       -> key = userId
     */
    public static String auditSmartKey(ActivityEvent e) {
        return switch (e.eventType()) {
            case PHOTO_ADDED, PHOTO_UPDATED, PHOTO_DELETED, LIKE_ADDED, LIKE_REMOVED ->
                    e.photoId() != null ? e.photoId().toString() : auditKeyByUser(e.userId());
            default -> auditKeyByUser(e.userId());
        };
    }
}
