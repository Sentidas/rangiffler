package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.data.entity.photo.LikeEntity;

import java.util.Date;
import java.util.UUID;

public record Like(
        UUID photoId,
        UUID userId,
        Date creationDate

) {
    public static Like fromEntity(LikeEntity likeEntity) {
        return new Like(
                likeEntity.getId().getPhotoId(),
                likeEntity.getId().getUserId(),
                likeEntity.getCreationDate()
        );
    }
}
