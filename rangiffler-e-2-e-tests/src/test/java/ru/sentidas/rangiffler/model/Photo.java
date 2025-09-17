package ru.sentidas.rangiffler.model;

import com.google.protobuf.util.Timestamps;

import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;
import ru.sentidas.rangiffler.grpc.PhotoResponse;
import ru.sentidas.rangiffler.grpc.UpdatePhotoRequest;

import javax.annotation.Nonnull;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public record Photo(
        UUID id,
        UUID userId,
        String src,
        String countryCode,
        String description,
        Date creationDate,
        int likesTotal
) {

    public Photo withLikesTotal(int newTotal) {
        return new Photo(id, userId, src, countryCode, description, creationDate, newTotal);
    }

    public static Photo fromEntity(PhotoEntity photoEntity) {

        String srcDataUrl = null;
        if (photoEntity.getPhoto() != null) {
            // Конвертируем byte[] в Data URL
            String base64src = Base64.getEncoder().encodeToString(photoEntity.getPhoto());
            srcDataUrl = "data:image/png;base64," + base64src;
        }

        return new Photo(
                photoEntity.getId(),
                photoEntity.getUser(),
                srcDataUrl,
                photoEntity.getCountryCode(),
                photoEntity.getDescription(),
                photoEntity.getCreatedDate(),
                0
        );
    }

    public void toProto(PhotoResponse.Builder b) {
        if (id != null) b.setPhotoId(id.toString());
        if (userId != null) b.setUserId(userId.toString());
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
                null,
                (request.hasSrc() && !request.getSrc().isBlank()) ? request.getSrc() : null,
                (request.hasCountryCode() && !request.getCountryCode().isBlank()) ? request.getCountryCode() : null,
                (request.hasDescription() && !request.getDescription().isBlank()) ? request.getDescription() : null,
     null,
        0);
    }
}

