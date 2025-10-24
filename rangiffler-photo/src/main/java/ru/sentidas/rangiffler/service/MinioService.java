package ru.sentidas.rangiffler.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoService.class);


    private final MinioClient minio;

    @Value("${app.media.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

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
            return objectKey;
        } catch (Exception e) {
            Throwable root = rootCause(e);
            LOG.error("MinIO upload failed: endpoint='{}', bucket='{}', objectKey='{}', cause='{}: {}'",
                    endpoint, bucket, objectKey, root.getClass().getSimpleName(), root.getMessage());

            throw new ru.sentidas.rangiffler.ex.StorageUnavailableException(
                    "MinIO is unavailable (endpoint=%s, bucket=%s)".formatted(endpoint, bucket), e);
        }
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

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}