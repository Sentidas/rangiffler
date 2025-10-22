package ru.sentidas.rangiffler.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minio;

    @Value("${app.media.bucket}")
    private String bucket;

    /**
     * Принимает data URL "data:image/...;base64,...."
     * Возвращает относительный путь (object key)
     */
    public String upload(UUID userId, byte[] bytes, String mime) throws Exception {
        String ext = switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported mime for upload: " + mime);
        };

        // photos/{userId}/{randomUuid}.{ext}
        String objectKey = "photos/%s/%s.%s".formatted(userId, UUID.randomUUID(), ext);

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(mime)
                            .stream(inputStream, bytes.length, -1)
                            .build()
            );
        }
        return objectKey;
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minio.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception ignore) {
            // best-effort: не роняем бизнес-операцию
        }
    }
}