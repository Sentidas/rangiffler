package ru.sentidas.rangiffler.service.impl;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.service.PhotoClient;
import ru.sentidas.rangiffler.utils.ImageHelper;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.PhotoDescriptions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class PhotoApiClient implements PhotoClient {

    private final RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub stub;

    public PhotoApiClient() {
        this.stub = GrpcChannels.photoBlockingStub;
    }

    @Override
    @Step("Get storage mode from photo service")
    @Nonnull
    public String getStorageMode() {
        PhotoStorageModeResponse response =
                stub.getStorageMode(com.google.protobuf.Empty.getDefaultInstance());
        return response.getMode(); // "BLOB" или "OBJECT"
    }

    @Override
    @Step("Get count photos user using API")
    public int getCountPhotos(UUID userId) {
        PhotoPageRequest request = PhotoPageRequest.newBuilder()
                .setUserId(userId.toString())
                .setPage(0)
                .setSize(1)
                .setIncludeTotal(true)
                .build();

        PhotosPageResponse response = stub.getUserPhotos(request);

        if (!response.hasTotal()) {
            throw new IllegalStateException("Response has no total despite include_total=true");
        }
        return response.getTotal();
    }

    @Override
    @Step("Get count (user + friends) photos using API")
    public int getFeedPhotos(UUID userId) {
        PhotoPageRequest request = PhotoPageRequest.newBuilder()
                .setUserId(userId.toString())
                .setPage(0)
                .setSize(1)
                .setIncludeTotal(true)
                .build();

        PhotosPageResponse response = stub.getFeedPhotos(request);

        if (!response.hasTotal()) {
            throw new IllegalStateException("Response has no total despite include_total=true");
        }
        return response.getTotal();
    }

    @Override
    @Step("Create photo using API (full model)")
    @Nonnull
    public AppPhoto createPhoto(AppPhoto photo) {
        CreatePhotoRequest request = CreatePhotoRequest.newBuilder()
                .setUserId(photo.userId().toString())
                .setSrc(photo.src())
                .setCountryCode(photo.countryCode())
                .setDescription(photo.description() == null ? "" : photo.description())
                .build();

        PhotoResponse response = stub.createPhoto(request);
        return toModel(response);
    }

    @Override
    @Step("Create photo using API (userId + optional country/path/description)")
    @Nonnull
    public AppPhoto createPhoto(UUID userId,
                                @Nullable String countryCode,
                                @Nullable String path,
                                @Nullable String description) {

        CreatePhotoRequest.Builder builder = CreatePhotoRequest.newBuilder()
                .setUserId(userId.toString());

        String resolvedCountry = (countryCode == null || countryCode.isBlank())
                ? GenerationDataUser.randomCountryCode()
                : countryCode;
        builder.setCountryCode(resolvedCountry);

        String resolvedSrc = (path == null || path.isBlank())
                ? GenerationDataUser.randomPhotoDataUrl()               // data:image/... из ресурсов
                : ImageHelper.fromClasspath(path).toDataUrl();          // data:image/... из classpath
        builder.setSrc(resolvedSrc);

        String resolvedDescription = (description == null || description.isBlank())
                ? PhotoDescriptions.pickRandom("en")
                : description;
        builder.setDescription(resolvedDescription);

        PhotoResponse response = stub.createPhoto(builder.build());
        return toModel(response);
    }

    @Override
    @Step("Create photo using API (userId + optional country)")
    @Nonnull
    public AppPhoto createPhoto(UUID userId, @Nullable String countryCode) {
        return createPhoto(userId, countryCode, null, null);
    }

    @Override
    @Step("Update photo using API (full model)")
    @Nonnull
    public AppPhoto updatePhoto(AppPhoto photo) {
        UpdatePhotoRequest.Builder builder = UpdatePhotoRequest.newBuilder()
                .setPhotoId(photo.id().toString())
                .setRequesterId(photo.userId().toString());

        // Меняем изображение только если пришёл dataUrl; URL игнорируем (менялись не-картинные поля)
        if (isDataUrl(photo.src())) {
            builder.setSrc(photo.src());
        }
        if (photo.countryCode() != null) {
            builder.setCountryCode(photo.countryCode());
        }
        if (photo.description() != null) {
            builder.setDescription(photo.description());
        }

        PhotoResponse response = stub.updatePhoto(builder.build());
        return toModel(response);
    }

    @Override
    @Step("Update photo using API (by ids + fields)")
    @Nonnull
    public AppPhoto updatePhoto(UUID photoId,
                                UUID userId,
                                @Nullable String dataUrlOrUrl,
                                @Nullable String countryCode,
                                @Nullable String description) {

        UpdatePhotoRequest.Builder builder = UpdatePhotoRequest.newBuilder()
                .setPhotoId(photoId.toString())
                .setRequesterId(userId.toString());

        // Только при dataUrl реально обновляем контент
        if (isDataUrl(dataUrlOrUrl)) {
            builder.setSrc(dataUrlOrUrl);
        }
        if (countryCode != null) {
            builder.setCountryCode(countryCode);
        }
        if (description != null) {
            builder.setDescription(description);
        }

        PhotoResponse response = stub.updatePhoto(builder.build());
        return toModel(response);
    }

    @Override
    @Step("Update photo using API (only country)")
    @Nonnull
    public AppPhoto updatePhoto(UUID photoId, UUID userId, @Nullable String countryCode) {
        return updatePhoto(photoId, userId, null, countryCode, null);
    }

    @Override
    @Step("Delete photo using API")
    public void deletePhoto(UUID photoId, UUID userId) {
        DeletePhotoRequest request = DeletePhotoRequest.newBuilder()
                .setId(photoId.toString())
                .setRequesterId(userId.toString())
                .build();
        stub.deletePhoto(request);
    }

    @Override
    @Nonnull
    public AppPhoto likePhoto(UUID photoId, UUID userId) {
        PhotoResponse response = stub.toggleLike(
                LikeRequest.newBuilder()
                        .setPhotoId(photoId.toString())
                        .setUserId(userId.toString())
                        .build()
        );
        return toModel(response);
    }

    @Nonnull
    private AppPhoto toModel(PhotoResponse response) {
        return new AppPhoto(
                UUID.fromString(response.getPhotoId()),
                UUID.fromString(response.getUserId()),
                response.getSrc(),
                response.getCountryCode(),
                response.getDescription(),
                null,
                response.hasLikes() ? response.getLikes().getTotal() : 0
        );
    }

    private static boolean isDataUrl(String value) {
        return value != null && value.startsWith("data:image");
    }
}
