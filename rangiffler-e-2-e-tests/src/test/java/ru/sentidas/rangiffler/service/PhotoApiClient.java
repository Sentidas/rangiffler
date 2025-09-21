package ru.sentidas.rangiffler.service;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.repository.impl.PhotoRepository;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.DeletePhoto;
import ru.sentidas.rangiffler.model.Photo;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class PhotoApiClient {

    private static final Config CFG = Config.getInstance();
    private final RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub stub;
    private final Long defaultDeadlineMs = 20_000L;

    private final PhotoRepository photoRepository = new PhotoRepository();

    public PhotoApiClient() {
        this.stub = RangifflerPhotoServiceGrpc.newBlockingStub(GrpcChannels.photoChannel);
    }

    @Step("Create spend using API")
    @Nonnull
    public Photo createPhoto(Photo photo) {

        CreatePhotoRequest request = CreatePhotoRequest.newBuilder()
                .setUserId(photo.userId().toString())
                .setSrc(photo.src())
                .setCountryCode(photo.countryCode())
                .setDescription(photo.description() == null ? "" : photo.description())
                .build();

        PhotoResponse response = call().createPhoto(request);
        return toModel(response);
    }

    @Step("Update spend using API")
    @Nonnull
    public Photo updatePhoto(Photo photo) {

        UpdatePhotoRequest.Builder builder = UpdatePhotoRequest.newBuilder()
                .setPhotoId(photo.id().toString())
                .setRequesterId(photo.userId().toString());

        if (photo.src() != null && !photo.src().isBlank()) {
            builder.setSrc(photo.src()); // новый контент
        }

        if (photo.countryCode() != null) {
            builder.setCountryCode(photo.countryCode());
        }
        if (photo.description() != null) {
            builder.setDescription(photo.description());
        }

        PhotoResponse response = call().updatePhoto(builder.build());
        return toModel(response);
    }

    @Step("Delete spend using API")
    public void deletePhoto(DeletePhoto photo) {

        DeletePhotoRequest request = DeletePhotoRequest.newBuilder()
                .setId(photo.photoId().toString())
                .setRequesterId(photo.userId().toString())
                .build();

        call().deletePhoto(request);
    }

    @Nonnull
    public ru.sentidas.rangiffler.model.Photo likePhoto(UUID photoId, UUID userId) {
        // 1) Один раз переключаем
        var resp = call().toggleLike(
                ru.sentidas.rangiffler.grpc.LikeRequest.newBuilder()
                        .setPhotoId(photoId.toString())
                        .setUserId(userId.toString())
                        .build()
        );

        // 2) Проверяем: пользователь появился в списке лайков?
        boolean liked = resp.hasLikes()
                && resp.getLikes().getLikesList().stream()
                .anyMatch(l -> l.getUserId().equals(userId.toString()));
        if (liked) {
            // Было “без лайка” → стало “с лайком”: готово
            return toModel(resp);
        }

        // 3) Идемпотентность: если уже был лайк и мы его сняли первым вызовом —
        // вернём в состояние “с лайком” вторым вызовом.
        var resp2 = call().toggleLike(
                ru.sentidas.rangiffler.grpc.LikeRequest.newBuilder()
                        .setPhotoId(photoId.toString())
                        .setUserId(userId.toString())
                        .build()
        );
        return toModel(resp2);
    }

    private Photo toModel(PhotoResponse response) {
        Photo photo = new Photo(
                UUID.fromString(response.getPhotoId()),
                UUID.fromString(response.getUserId()),
                response.getSrc(),
                response.getCountryCode(),
                response.getDescription(),
                null,
                0
        );
        return photo;

    }

    private String detectContentType(Path path) {
        try {
            String p = java.nio.file.Files.probeContentType(path);
            if (p != null && !p.isBlank()) return p;
        } catch (Exception ignored) {
        }
        String n = path.getFileName().toString().toLowerCase();
        if (n.endsWith(".jpg") || n.endsWith(".jpeg")) return "image/jpeg";
        if (n.endsWith(".png")) return "image/png";
        if (n.endsWith(".webp")) return "image/webp";
        if (n.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }

    private static String toDataUrl(java.nio.file.Path file, String contentType) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file);
            String b64 = java.util.Base64.getEncoder().encodeToString(bytes);
            return "data:" + contentType + ";base64," + b64;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }

    private RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub call() {
        if (defaultDeadlineMs == null) return stub; // без дедлайна
        return stub.withDeadline(io.grpc.Deadline.after(defaultDeadlineMs, java.util.concurrent.TimeUnit.MILLISECONDS));
    }
}
