package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.ImageFormatValidator;
import ru.sentidas.rangiffler.InvalidImageFormatException;
import ru.sentidas.rangiffler.MediaProperties;
import ru.sentidas.rangiffler.model.StorageType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.UUID;
/**
 * Сервис обработки и сохранения аватаров.
 *
 * Назначение:
 * - Использует общую библиотеку
 *   rangiffler-media-common (DataUrl, ImageFormatValidator, MediaProperties)
 *   для парсинга и валидации изображения (тип + формат dataURL);
 * - Применяет единый лимит на размер (19 МБ) после декодирования Base64;
 * - Генерирует миниатюру PNG (100x100) через Thumbnailator;
 * - Выполняет запись либо в объектное хранилище (MinIO), либо в БД (BLOB)
 *   в зависимости от целевого режима;
 * - Безопасно удаляет старый объект (best effort) при смене ключа.
 * - Вызывается из UserService при updateUser.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AvatarService {

    /** Максимально допустимый размер изображения в байтах (19 МБ). */
    public static final int MAX_IMAGE_BYTES = 19 * 1024 * 1024;

    /** Размер миниатюры (квадрат). */
    public static final int AVATAR_SMALL = 100;

    private final AvatarMinioService minioService;
    private final MediaProperties mediaProperties;

    /**
     * Результат обработки аватара для сохранения в UserEntity.
     * Для OBJECT: objectKey != null, avatar(bytes) = null.
     * Для BLOB: objectKey = null, avatar(bytes) != null.
     */
    public record Result(
            @Nullable String objectKey,
            @Nullable byte[] avatarBytes,
            String mime,
            @Nullable byte[] avatarSmallBytes,
            @Nullable String deleteOldObjectKey // если не null — старый ключ надо удалить (best effort)
    ) {}

    /**
     * Обрабатывает data URL и готовит набор полей под требуемый режим хранения.
     *
     * @param userId          идентификатор пользователя (для пути в MinIO)
     * @param avatarDataUrl   строка data URL (обязательна)
     * @param target          целевой режим хранения (BLOB или OBJECT)
     * @param oldObjectKey    прежний ключ в объектном хранилище (если был), чтобы удалить при смене
     * @return Result для записи в UserEntity
     *
     * Бросает:
     * - InvalidImageFormatException (неверный формат/MIME/размер)
     * - RuntimeException при ошибках чтения/записи объектов/миниатюры.
     *
     */
    public Result process(UUID userId,
                          String avatarDataUrl,
                          StorageType target,
                          @Nullable String oldObjectKey) {

        // 1) Валидация dataURL и MIME через общую библиотеку
        Set<String> allowed = mediaProperties.getAllowedMime();
        ImageFormatValidator validator = new ImageFormatValidator(allowed);
        validator.validateDataUrlOrThrow(avatarDataUrl);
        final String mime = validator.extractMimeOrThrow(avatarDataUrl);

        // 2) Парсинг bytes через общий DataUrl
        final DataUrl data = DataUrl.parse(avatarDataUrl);
        final byte[] bytes = data.bytes();

        // 3) Лимит размера 19 МБ (после декодирования Base64)
        if (bytes == null || bytes.length == 0) {
            throw new InvalidImageFormatException("Empty image content", allowed);
        }
        if (bytes.length > MAX_IMAGE_BYTES) {
            throw new InvalidImageFormatException("Image is too large (> 19MB)", allowed);
        }

        // 4) Миниатюра PNG 100x100
        final byte[] small = generateSmallPng(bytes, allowed);

        // 5) OBJECT → кладём оригинал в MinIO, чистим старый ключ, если меняется
        if (target == StorageType.OBJECT) {
            final String newKey;
            try {
                newKey = minioService.upload(userId, bytes, mime);
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload avatar to MinIO", e);
            }

            final String toDelete =
                    (oldObjectKey != null && !oldObjectKey.isBlank() && !oldObjectKey.equals(newKey))
                            ? oldObjectKey
                            : null;

            return new Result(newKey, null, mime, small, toDelete);
        }

        // 6) BLOB → сохраняем в БД, старый объект (если был) помечаем к удалению
        return new Result(null, bytes, mime, small, oldObjectKey);
    }

    /**
     * Удаление объекта из MinIO (best effort).
     * @param objectKey ключ объекта
     */
    public void deleteObjectIfNeeded(@Nullable String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minioService.deleteObject(objectKey);
        } catch (Exception ignore) {
            log.warn("MinIO deleteObject best-effort failed for key={}", objectKey);
        }
    }

    /**
     * Генерирует PNG-миниатюру 100x100 с сохранением пропорций.
     *
     * @param originalBytes байты исходного изображения
     * @param allowed       список разрешённых MIME (для формирования понятной ошибки)
     * @return PNG-байты миниатюры
     */
    private static byte[] generateSmallPng(byte[] originalBytes, Set<String> allowed) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            final BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new InvalidImageFormatException("Unsupported image content", allowed);
            }

            net.coobird.thumbnailator.Thumbnails.of(source)
                    .size(AVATAR_SMALL, AVATAR_SMALL)
                    .outputFormat("png")
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();
        } catch (InvalidImageFormatException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PNG thumbnail", e);
        }
    }
}
