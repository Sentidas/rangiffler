package ru.sentidas.rangiffler.model;

import com.google.protobuf.util.Timestamps;
import jakarta.annotation.Nonnull;
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
        // OBJECT → наружу идёт КЛЮЧ; gateway склеит http://.../media/<key>

        if (photoEntity.getStorage() == StorageType.OBJECT) {
            src = photoEntity.getPhotoUrl(); // ключ (как сейчас)
        } else { // BLOB
            byte[] bytes = photoEntity.getPhoto();
            if (bytes != null && bytes.length > 0) {
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                src = "data:" + photoEntity.getPhotoMime() + ";base64," + base64; // data URL для фронта
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

