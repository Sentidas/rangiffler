package ru.sentidas.rangiffler.service.impl;

import io.qameta.allure.Step;
import org.jetbrains.annotations.NotNull;
import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;
import ru.sentidas.rangiffler.data.repository.impl.PhotoRepositoryImpl;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.service.PhotoClient;
import ru.sentidas.rangiffler.utils.ImageHelper;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.PhotoDescriptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class PhotoDbClient implements PhotoClient {

    private static final Config CFG = Config.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.photoJdbcUrl()
    );

    private final PhotoRepositoryImpl photoRepository = new PhotoRepositoryImpl();

    // Для OBJECT (MinIO/URL) и для счётчиков делегируем в API
    private final PhotoApiClient api = new PhotoApiClient();
    private final String storageMode;

    public PhotoDbClient() {
        this.storageMode = api.getStorageMode();
    }

    @Override
    public String getStorageMode() {
        return storageMode;
    }

    @Override
    @Step("Get count photos using API (delegated)")
    public int getCountPhotos(UUID userId) {
        return api.getCountPhotos(userId);
    }

    @Override
    @Step("Get feed photos count using API (delegated)")
    public int getFeedPhotos(UUID userId) {
        return api.getFeedPhotos(userId);
    }


    @Override
    @Step("Create photo (DB or delegate to API for OBJECT)")
    @Nonnull
    public AppPhoto createPhoto(AppPhoto photo) {
        if ("OBJECT".equalsIgnoreCase(storageMode)) {
            // т.к. нужен MinIO/URL — создаём через сервис
            return api.createPhoto(photo);
        }
        // === BLOB ===
        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> photo.fromEntity(
                                photoRepository.create(
                                        PhotoEntity.from(photo)
                                )
                        )
                )
        );
    }

    @Override
    @Step("Create photo (fields) (DB or delegate to API for OBJECT)")
    @Nonnull
    public AppPhoto createPhoto(UUID userId,
                                @Nullable String countryCode,
                                @Nullable String path,
                                @Nullable String description) {
        if ("OBJECT".equalsIgnoreCase(storageMode)) {
            // делегируем сервису: он примет dataUrl и вернёт URL
            return api.createPhoto(userId, countryCode, path, description);
        }

        // === BLOB ===
        final String code = (countryCode == null || countryCode.isBlank())
                ? GenerationDataUser.randomCountryCode()
                : countryCode;

        final String src = (path == null || path.isBlank())
                ? GenerationDataUser.randomPhotoDataUrl()
                : ImageHelper.fromClasspath(path).toDataUrl();

        final String desc = (description == null || description.isBlank())
                ? PhotoDescriptions.pickRandom("en")
                : description;

        final AppPhoto model = new AppPhoto(null, userId, src, code, desc, new Date(), 0);

        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> model.fromEntity(
                                photoRepository.create(
                                        PhotoEntity.from(model)
                                )
                        )
                )
        );
    }

    @Override
    @Step("Create photo (userId + optional country)")
    @Nonnull
    public AppPhoto createPhoto(UUID userId, @Nullable String countryCode) {
        return createPhoto(userId, countryCode, null, null);
    }

    @Override
    @Step("Update photo (full model) (BLOB: DB, OBJECT: dataUrl→API, URL→DB metadata)")
    @Nonnull
    public AppPhoto updatePhoto(AppPhoto photo) {
        if ("OBJECT".equalsIgnoreCase(storageMode)) {
            if (isDataUrl(photo.src())) {
                // Реальная замена изображения — через сервис, он вернёт URL
                return api.updatePhoto(photo);
            }
            // URL или null: меняем только метаданные в БД (без рекурсии)
            return requireNonNull(
                    xaTransactionTemplate.execute(() -> {
                        PhotoEntity current = photoRepository.findById(photo.id())
                                .orElseThrow(() -> new IllegalArgumentException("Photo does not exist: " + photo.id()));
                        PhotoEntity toUpdate = new PhotoEntity();
                        toUpdate.setId(current.getId());
                        toUpdate.setUser(current.getUser());
                        toUpdate.setCreatedDate(current.getCreatedDate());
                        toUpdate.setStorage(current.getStorage());
                        toUpdate.setPhoto(current.getPhoto());
                        toUpdate.setPhotoUrl(current.getPhotoUrl()); // src не трогаем
                        toUpdate.setCountryCode(photo.countryCode() != null ? photo.countryCode() : current.getCountryCode());
                        toUpdate.setDescription(photo.description() != null ? photo.description() : current.getDescription());
                        return AppPhoto.fromEntity(photoRepository.update(toUpdate));
                    })
            );
        }

        // BLOB
        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> AppPhoto.fromEntity(
                                photoRepository.update(PhotoEntity.from(photo))
                        )
                )
        );
    }

    @Override
    @Step("Update photo by ids (BLOB: DB, OBJECT: dataUrl→API, URL→DB metadata)")
    @Nonnull
    public AppPhoto updatePhoto(UUID photoId,
                                UUID userId,
                                @Nullable String dataUrlOrUrl,
                                @Nullable String countryCode,
                                @Nullable String description) {

        if ("OBJECT".equalsIgnoreCase(storageMode)) {
            if (isDataUrl(dataUrlOrUrl)) {
                return api.updatePhoto(photoId, userId, dataUrlOrUrl, countryCode, description);
            }
            // URL или null → были обновления по коду страны или описанию
            return requireNonNull(
                    xaTransactionTemplate.execute(() -> {
                        PhotoEntity current = photoRepository.findById(photoId)
                                .orElseThrow(() -> new IllegalArgumentException("Photo does not exist: " + photoId));
                        PhotoEntity toUpdate = new PhotoEntity();
                        toUpdate.setId(current.getId());
                        toUpdate.setUser(current.getUser());
                        toUpdate.setCreatedDate(current.getCreatedDate());
                        toUpdate.setStorage(current.getStorage());
                        toUpdate.setPhoto(current.getPhoto());
                        toUpdate.setPhotoUrl(current.getPhotoUrl());
                        toUpdate.setCountryCode(countryCode == null ? current.getCountryCode() : countryCode);
                        toUpdate.setDescription(description == null ? current.getDescription() : description);
                        return AppPhoto.fromEntity(photoRepository.update(toUpdate));
                    })
            );
        }

        // BLOB
        AppPhoto current = requireNonNull(
                xaTransactionTemplate.execute(
                        () -> AppPhoto.fromEntity(
                                photoRepository.findById(photoId)
                                        .orElseThrow(() -> new IllegalArgumentException("Photo does not exist: " + photoId))
                        )
                )
        );

        String nextSrc = (dataUrlOrUrl == null || dataUrlOrUrl.isBlank())
                ? current.src()
                : dataUrlOrUrl;

        // Если пришёл dataUrl — валидируем формат (PhotoEntity.from(updated) положит байты в BLOB)
        if (isDataUrl(nextSrc)) {
            DataUrl.parse(nextSrc);
        }

        AppPhoto updated = new AppPhoto(
                photoId,
                userId,
                nextSrc,
                countryCode == null ? current.countryCode() : countryCode,
                description == null ? current.description() : description,
                current.creationDate(),
                current.likesTotal()
        );

        return requireNonNull(
                xaTransactionTemplate.execute(
                        () -> AppPhoto.fromEntity(
                                photoRepository.update(PhotoEntity.from(updated))
                        )
                )
        );
    }

    @Override
    @Step("Update photo (only country)")
    @Nonnull
    public AppPhoto updatePhoto(UUID photoId, UUID userId, @Nullable String countryCode) {
        return updatePhoto(photoId, userId, null, countryCode, null);
    }


    @Override
    @Step("Delete photo using SQL by id")
    public void deletePhoto(UUID photoId, UUID userId) {
        xaTransactionTemplate.execute(() -> {
            Optional<PhotoEntity> maybeExisting = photoRepository.findById(photoId);
            if (maybeExisting.isPresent()) {
                photoRepository.remove(maybeExisting.get());
            } else {
                throw new IllegalArgumentException("Photo does not exist: " + photoId);
            }
            return null;
        });
    }

    @Override
    public AppPhoto likePhoto(UUID photoId, UUID userId) {
        throw new UnsupportedOperationException("DB client does not provide paging; use API client");
    }

    private static boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:image");
    }
}
