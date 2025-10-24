package ru.sentidas.rangiffler.service;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.Like;

import java.util.UUID;

public interface PhotoClient {

    @Step("Get storage mode from photo service")

    String getStorageMode();

    int getCountPhotos(UUID userId);

    int getFeedPhotos(UUID userId);

    AppPhoto createPhoto(AppPhoto photo);

    AppPhoto createPhoto(UUID userId, String countryCode, String path, String description);

    AppPhoto createPhoto(UUID userId, String countryCode);

    AppPhoto updatePhoto(AppPhoto photo);

    AppPhoto updatePhoto(UUID photoId, UUID userId, String dataUrlOrUrl, String countryCode, String description);

    AppPhoto updatePhoto(UUID photoId, UUID userId, String countryCode);

    void deletePhoto(UUID photoId, UUID userId);

    AppPhoto likePhoto(UUID photoId, UUID userId);
}
