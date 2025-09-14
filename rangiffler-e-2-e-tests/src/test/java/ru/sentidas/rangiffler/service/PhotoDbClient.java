package ru.sentidas.rangiffler.service;

import io.qameta.allure.Step;
import org.jetbrains.annotations.NotNull;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;
import ru.sentidas.rangiffler.data.repository.impl.PhotoRepository;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class PhotoDbClient {

    private static final Config CFG = Config.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.photoJdbcUrl()
    );

    private final PhotoRepository photoRepository = new PhotoRepository();

    @Step("Create spend using SQL INSERT")
    @Nonnull
    public Photo createPhoto(Photo photo) {
        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> Photo.fromEntity(
                                photoRepository.create(
                                        PhotoEntity.fromJson(photo)
                                )
                        )
                )
        );
    }


    @Step("Update category using SQL UPDATE")
    @NotNull
    public Photo updatePhoto(Photo photo) {
        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> Photo.fromEntity(
                                photoRepository.update(
                                        PhotoEntity.fromJson(photo)
                                )
                        )
                )
        );
    }


    @Step("Remove category using SQL DELETE")
    public void removePhoto(Photo photo) {
        xaTransactionTemplate.execute(() -> {
                    Optional<PhotoEntity> deletedPhoto = photoRepository
                            .findById(photo.id());
                    if (deletedPhoto.isPresent()) {
                        photoRepository.remove(deletedPhoto.get());
                    } else {
                        throw new IllegalArgumentException("Photo does not exists: " + photo.id());
                    }
                    return null;
                }
        );
    }

    @Step("Create like for photo by friend SQL INSERT")
    @Nonnull
    public Like likePhoto(UUID photoId, UUID userId) {
        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> Like.fromEntity(
                                photoRepository.like(
                                        photoId, userId)
                        )
                )
        );
    }
}
