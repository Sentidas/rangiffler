package ru.sentidas.rangiffler.service.api;

import com.google.protobuf.util.Timestamps;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.input.PhotoInput;
import ru.sentidas.rangiffler.service.utils.GrpcCall;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@Component
public class GrpcPhotoClient {

    private static final String SERVICE = "rangiffler-photo";

    private final GrpcUserdataClient grpcUserdataClient;

    // Базовый публичный адрес gateway для сборки абсолютных URL, например: http://127.0.0.1:8081
    private static String BASE_URL = "";

    @Autowired
    public GrpcPhotoClient(
            GrpcUserdataClient grpcUserdataClient,
            @Value("${app.public-base-url:}") String baseUrl
    ) {
        this.grpcUserdataClient = grpcUserdataClient;
        // убрать хвостовые слэши и сохранить в статике для использования в статических мапперах
        BASE_URL = baseUrl == null ? "" : baseUrl.replaceAll("/+$", "");
    }

    @GrpcClient("grpcPhotoClient")
    private RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub stub;

    public Photo createPhoto(String username, PhotoInput photoInput) {
        final String userId = grpcUserdataClient.currentUser(username).id().toString();

        if (photoInput.src() == null || photoInput.src().isBlank()) {
            throw new IllegalArgumentException("`src` is required for create");
        }
        if (photoInput.country() == null || photoInput.country().code() == null || photoInput.country().code().isBlank()) {
            throw new IllegalArgumentException("`country.code` is required for create");
        }


        CreatePhotoRequest.Builder request = CreatePhotoRequest.newBuilder()
                .setUserId(userId)
                .setSrc(photoInput.src())
                .setCountryCode(photoInput.country().code());

        if (photoInput.description() != null) {
            request.setDescription(photoInput.description());
        }
        PhotoResponse response = GrpcCall.run(() -> stub.createPhoto(request.build()), SERVICE);
        return fromProto(response);
    }

    public Photo updatePhoto(String username, PhotoInput photoInput) {
        UpdatePhotoRequest.Builder request = UpdatePhotoRequest.newBuilder()
                .setPhotoId(photoInput.id().toString());
        request.setRequesterId(grpcUserdataClient
                .currentUser(username).id().toString());

        if (photoInput.description() != null) {
            request.setDescription(photoInput.description());
        }

        if (photoInput.src() != null) {
            request.setSrc(photoInput.src());
        }



        if (photoInput.country() != null && photoInput.country().code() != null) {
            request.setCountryCode(photoInput.country().code());

        }

        PhotoResponse response = GrpcCall.run(() -> stub.updatePhoto(request.build()), SERVICE);
        return fromProto(response);
    }

    public boolean deletePhoto(String requesterUsername, UUID photoId) {

        UUID requesterId = grpcUserdataClient.getIdByUsername(requesterUsername);
        if (requesterId == null) {
            throw new IllegalStateException("User id is null for username=" + requesterUsername);
        }

        DeletePhotoRequest request = DeletePhotoRequest.newBuilder()
                .setId(photoId.toString())
                .setRequesterId(requesterId.toString())
                .build();
        GrpcCall.run(() -> stub.deletePhoto(request), SERVICE);
        return true;

    }

    public Slice<Photo> userPhotos(String username, Pageable pageable) {
        final String userId = grpcUserdataClient.currentUser(username).id().toString();

        PhotoPageRequest request = PhotoPageRequest.newBuilder()
                .setPage(pageable.getPageNumber())
                .setSize(pageable.getPageSize())
                .setUserId(userId)
                .build();



        PhotosPageResponse response = GrpcCall.run(() -> stub.getUserPhotos(request), SERVICE);
        return fromProto(response);
    }

    public Slice<Photo> feedPhotos(String username, Pageable pageable) {
        final String userId = grpcUserdataClient.currentUser(username).id().toString();

        PhotoPageRequest request = PhotoPageRequest.newBuilder()
                .setPage(pageable.getPageNumber())
                .setSize(pageable.getPageSize())
                .setUserId(userId)
                .build();

        PhotosPageResponse response = GrpcCall.run(() -> stub.getFeedPhotos(request), SERVICE);
        return fromProto(response);
    }

    public Photo toggleLike(String username, UUID photoId) {
        final String userId = grpcUserdataClient.currentUser(username).id().toString();

        LikeRequest request = LikeRequest.newBuilder()
                .setPhotoId(photoId.toString())
                .setUserId(userId)
                .build();

        PhotoResponse response = GrpcCall.run(() -> stub.toggleLike(request), SERVICE);
        return fromProto(response);
    }


    public static Slice<Photo> fromProto(PhotosPageResponse response) {
        List<Photo> content = response.getContentList()
                .stream()
                .map(GrpcPhotoClient::fromProto)
                .toList();

        boolean hasNext = !response.getLast();
        return new SliceImpl<>(content,
                PageRequest.of(response.getPage(), response.getSize()),
                hasNext
        );
    }

    public static Photo fromProto(PhotoResponse response) {
        Date createdDate = new Date(Timestamps.toMillis(response.getCreationDate()));

        List<Like> likeList = response.hasLikes()
                ? response.getLikes().getLikesList().stream().map(GrpcPhotoClient::mapLike).toList()
                : List.of();

        int total = response.hasLikes()
                ? response.getLikes().getTotal()
                : 0;

        // В gRPC src = относительный ключ (object key) из БД, например "photos/.../file.png"
        String src = response.getSrc();
        if (src != null && src.startsWith("data:")) {
            // оставить как есть — НЕ заменять на application/octet-stream
        } else if (src != null && !src.isBlank()) {
            // OBJECT режим — собрать абсолютный URL
            src = (BASE_URL != null && !BASE_URL.isBlank())
                    ? BASE_URL + "/media/" + src
                    : "/media/" + src;
        }

        ru.sentidas.rangiffler.model.Likes likesWrapper = new ru.sentidas.rangiffler.model.Likes(total, likeList);

        return new Photo(
                UUID.fromString(response.getPhotoId()),
                src,
                response.getCountryCode(),
                response.getDescription(),
                createdDate,
                likesWrapper
        );
    }

    public static Like mapLike(ru.sentidas.rangiffler.grpc.Like like) {
        Date createdDate = new Date(Timestamps.toMillis(like.getCreationDate()));

        return new Like(
                UUID.fromString(like.getUserId()),
                "",
                createdDate
        );
    }
}
