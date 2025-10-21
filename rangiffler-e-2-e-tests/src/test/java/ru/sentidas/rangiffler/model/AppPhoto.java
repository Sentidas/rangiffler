package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public record AppPhoto(
        UUID id,
        UUID userId,
        String src,
        String countryCode,
        String description,
        Date creationDate,
        int likesTotal
) {

    public AppPhoto withLikesTotal(int newTotal) {
        return new AppPhoto(id, userId, src, countryCode, description, creationDate, newTotal);
    }

    public static AppPhoto fromEntity(PhotoEntity photoEntity) {
        String srcValue;

        if (photoEntity.getPhotoUrl() != null && !photoEntity.getPhotoUrl().isBlank()) {
            // новый режим: ключ (relative path), например "photos/.../file.png"
            srcValue = photoEntity.getPhotoUrl();
        } else if (photoEntity.getPhoto() != null) {
            // старый режим: data URL из BLOB
            String base64src = Base64.getEncoder().encodeToString(photoEntity.getPhoto());
            srcValue = "data:image/png;base64," + base64src;
        } else {
            srcValue = null;
        }

        return new AppPhoto(
                photoEntity.getId(),
                photoEntity.getUser(),
                srcValue,
                photoEntity.getCountryCode(),
                photoEntity.getDescription(),
                photoEntity.getCreatedDate(),
                0
        );
    }
}

