package ru.sentidas.rangiffler.model;

import com.google.protobuf.util.Timestamps;
import jakarta.annotation.Nonnull;
import ru.sentidas.rangiffler.data.StorageType;
import ru.sentidas.rangiffler.data.entity.PhotoEntity;
import ru.sentidas.rangiffler.grpc.PhotoResponse;
import ru.sentidas.rangiffler.grpc.UpdatePhotoRequest;

import java.util.Date;
import java.util.UUID;

public record Photo(
        UUID id,
        UUID requesterId,
        String src,
        String countryCode,
        String description,
        Date creationDate,
        int likesTotal
) {

    public static Photo fromEntity(PhotoEntity photoEntity) {
        String src = null;

        if (photoEntity.getStorage() == StorageType.OBJECT) {
            src = photoEntity.getPhotoUrl(); // ключ (как сейчас)
        } else if (photoEntity.getStorage() == StorageType.BLOB) {
            byte[] bytes = photoEntity.getPhoto();
            if (bytes != null && bytes.length > 0) {
                String mime = guessMime(bytes); // простая эвристика
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                src = "data:" + mime + ";base64," + base64; // data URL для фронта
            }
        }

        return new Photo(
                photoEntity.getId(),
                photoEntity.getUser(),
                src,
                photoEntity.getCountryCode(),
                photoEntity.getDescription(),
                photoEntity.getCreatedDate(),
                0
        );
    }

    /** Очень простая авто-детекция MIME (jpeg/png/webp -> иначе octet-stream) */
    private static String guessMime(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 0x50 && b[2] == 0x4E && b[3] == 0x47) return "image/png";
        if (b.length >= 12 && b[0]=='R' && b[1]=='I' && b[2]=='F' && b[3]=='F' && b[8]=='W' && b[9]=='E' && b[10]=='B' && b[11]=='P') return "image/webp";
        return "application/octet-stream";
    }

    public void toProto(PhotoResponse.Builder b) {
        if (id != null) b.setPhotoId(id.toString());
        if (requesterId != null) b.setUserId(requesterId.toString());
        if (src != null) b.setSrc(src);
        if (description != null) b.setDescription(description);
        if (countryCode != null) b.setCountryCode(countryCode);
        if (creationDate != null) {
            b.setCreationDate(
                    Timestamps.fromMillis(creationDate.getTime())
            );
        }

        // --- ВАЖНО: лайки (хотя бы total, чтобы не было null) ---В Photo.toProto(...) лучше не ставить лайки вообще, раз ты их собираешь в gRPC-слое. Удали блок
//        b.setLikes(Likes.newBuilder()
//                .setTotal(likesTotal)
//                .build());
//    }
    }

    public static @Nonnull Photo fromProto(@Nonnull UpdatePhotoRequest request) {
        String photoId = request.getPhotoId();
        if (photoId == null || photoId.isBlank()) {
            throw new IllegalArgumentException("photoId is required");
        }

        return new Photo(
                UUID.fromString(photoId),
                UUID.fromString(request.getRequesterId()),
                (request.hasSrc() && !request.getSrc().isBlank()) ? request.getSrc() : null,
                (request.hasCountryCode() && !request.getCountryCode().isBlank()) ? request.getCountryCode() : null,
                (request.hasDescription() && !request.getDescription().isBlank()) ? request.getDescription() : null,
                null,
                0);

    }
}

