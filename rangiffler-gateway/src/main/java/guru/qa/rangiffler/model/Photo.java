package guru.qa.rangiffler.model;

import guru.qa.rangiffler.entity.PhotoEntity;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public record Photo(
        UUID id,
        String src,
        Country country,
        String description,
        Date createionDate,
        Likes likes
) {

    public static Photo fromPhotoEntity(PhotoEntity photoEntity) {

        String srcDataUrl = null;
        if (photoEntity.getPhoto() != null) {
            // Конвертируем byte[] в Data URL
            String base64src = Base64.getEncoder().encodeToString(photoEntity.getPhoto());
            srcDataUrl = "data:image/png;base64," + base64src;
        }

        return new Photo(
                photoEntity.getId(),
                srcDataUrl,
                Country.fromEntity(photoEntity.getCountry()),
                photoEntity.getDescription(),
                photoEntity.getCreatedDate(),
                null
        );
    }
}
