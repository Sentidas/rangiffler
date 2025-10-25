package ru.sentidas.rangiffler.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarMinioService {

    private static final Logger LOG = LoggerFactory.getLogger(AvatarMinioService.class);

    private final MinioClient minio;

    @Value("${app.media.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    public String upload(UUID userId, byte[] bytes, String mime){
        String ext = switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported mime for upload: " + mime);
        };

        String objectKey = "avatars/%s/%s.%s".formatted(userId, java.util.UUID.randomUUID(), ext);

        try (var is = new ByteArrayInputStream(bytes)) {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(mime)
                            .stream(is, bytes.length, -1)
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            Throwable root = rootCause(e);
            LOG.error("MinIO avatar upload failed: endpoint='{}', bucket='{}', objectKey='{}', cause='{}: {}'",
                    endpoint, bucket, objectKey, root.getClass().getSimpleName(),
                    root.getMessage());

            throw new ru.sentidas.rangiffler.ex.StorageUnavailableException(
                    "MinIO is unavailable (endpoint=%s, bucket=%s)".formatted(endpoint, bucket), e);
        }
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception ignore) {
        }
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur;
    }
}
