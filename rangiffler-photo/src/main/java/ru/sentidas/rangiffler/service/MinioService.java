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
     * Кладёт в бакет, возвращает относительный путь (object key), напр. "photos/{userId}/{photoId}/{uuid}.png".
     */
    public String uploadFromDataUrl(UUID userId, UUID photoId, String dataUrl) throws Exception {
        if (dataUrl == null || !dataUrl.startsWith("data:")) {
            throw new IllegalArgumentException("src must be data URL");
        }
        int comma = dataUrl.indexOf(',');
        if (comma < 0) throw new IllegalArgumentException("invalid data URL");

        String meta = dataUrl.substring(5, comma);                // image/png;base64
        String contentType = meta.replace(";base64", "");         // image/png
        byte[] bytes = Base64.getDecoder().decode(dataUrl.substring(comma + 1));

        String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> "bin";
        };

        String objectKey = "photos/%s/%s.%s".formatted(userId, UUID.randomUUID(), ext);

        try (var is = new ByteArrayInputStream(bytes)) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(contentType)
                            .stream(is, bytes.length, -1)
                            .build()
            );
        }
        return objectKey; // <-- ВОЗВРАЩАЕМ КЛЮЧ, НЕ ПОЛНЫЙ URL
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
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
