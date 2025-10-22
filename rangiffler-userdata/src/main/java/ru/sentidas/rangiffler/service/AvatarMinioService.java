package ru.sentidas.rangiffler.service;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvatarMinioService {

    private final MinioClient minio;

    @Value("${app.media.bucket}")
    private String bucket;

    public String upload(UUID userId, byte[] bytes, String mime) throws Exception {
        String ext = switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/gif"  -> "gif";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported mime for upload: " + mime);
        };

        String objectKey = "avatars/%s/%s.%s".formatted(userId, java.util.UUID.randomUUID(), ext);

        try (var is = new java.io.ByteArrayInputStream(bytes)) {
            minio.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(mime)
                            .stream(is, bytes.length, -1)
                            .build()
            );
        }
        return objectKey;
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minio.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception ignore) {
        }
    }
}
