package ru.sentidas.rangiffler.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.CreatePhoto;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.service.PhotoService;

import java.util.*;

@GrpcService
public class GrpcPhotoService extends RangifflerPhotoServiceGrpc.RangifflerPhotoServiceImplBase {


    private final PhotoService photoService;
    private final GrpcUserdataClient grpcUserdataClient;

    @Autowired
    public GrpcPhotoService(PhotoService photoService, GrpcUserdataClient grpcUserdataClient) {
        this.photoService = photoService;
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @Override
    public void createPhoto(CreatePhotoRequest request, StreamObserver<PhotoResponse> responseObserver) {
        String normalizedCountryCode = normalizeCountryCode(request.getCountryCode());

        CreatePhoto createdPhoto = new CreatePhoto(
                UUID.fromString(request.getUserId()),
                request.getSrc(),
                normalizedCountryCode,
                request.getDescription()
        );
        Photo photo = photoService.addPhoto(createdPhoto);
        responseObserver.onNext(toProto(photo));
        responseObserver.onCompleted();
    }

    @Override
    public void updatePhoto(UpdatePhotoRequest request, StreamObserver<PhotoResponse> responseObserver) {
        UpdatePhotoRequest normalizedRequest = request;

        if (request.hasCountryCode()) {
            String normalizedCountryCode = normalizeCountryCode(request.getCountryCode());
            normalizedRequest = UpdatePhotoRequest.newBuilder(request)
                    .setCountryCode(normalizedCountryCode)
                    .build();
        }

        Photo photo = Photo.fromProto(normalizedRequest);
        Photo updated = photoService.updatePhoto(photo);
        responseObserver.onNext(toProto(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void getUserPhotos(PhotoPageRequest request, StreamObserver<PhotosPageResponse> responseObserver) {
        UUID userId = UUID.fromString(request.getUserId());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<Photo> photos = photoService.userPhotos(userId, pageable);

        Integer total = null;
        if (request.getIncludeTotal()) {
            total = photoService.countUserPhotos(UUID.fromString(request.getUserId()));
        }
        responseObserver.onNext(toProtoWithBatchedLikes(photos, total));
        responseObserver.onCompleted();
    }


    @Override
    public void getFeedPhotos(PhotoPageRequest request, StreamObserver<PhotosPageResponse> responseObserver) {
        UUID userId = UUID.fromString(request.getUserId());
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<Photo> photos = photoService.feedPhotos(userId, pageable);

        Integer total = null;
        if (request.getIncludeTotal()) {
            total = photoService.countFeedPhotos(UUID.fromString(request.getUserId()));
        }

        responseObserver.onNext(toProtoWithBatchedLikes(photos, total));
        responseObserver.onCompleted();
    }

    @Override
    public void deletePhoto(DeletePhotoRequest request, StreamObserver<Empty> responseObserver) {
        photoService.deletePhoto(
                UUID.fromString(request.getRequesterId()),
                UUID.fromString(request.getId())
        );
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }


    @Override
    public void toggleLike(LikeRequest request, StreamObserver<PhotoResponse> out) {
        UUID requesterId = UUID.fromString(request.getUserId());
        UUID photoId = UUID.fromString(request.getPhotoId());

        // переключили лайк
        Photo photo = photoService.toggleLike(requesterId, photoId);

        // добрали актуальные лайки
        List<Like> likes = photoService.photoLikes(photoId);
        if (likes == null) {
            likes = List.of();
        }

        PhotoResponse.Builder builder = PhotoResponse.newBuilder();
        photo.toProto(builder);
        builder.setLikes(ru.sentidas.rangiffler.model.Like.toProtoList(likes));

        out.onNext(builder.build());
        out.onCompleted();
    }

    @Override
    public void getPhotoLikes(PhotoIdRequest request, StreamObserver<Likes> responseObserver) {
        UUID photoId = UUID.fromString(request.getId());
        List<Like> likes = photoService.photoLikes(photoId);
        if (likes == null) {
            likes = List.of();
        }

        responseObserver.onNext(Like.toProtoList(likes));
        responseObserver.onCompleted();
    }

    private String normalizeCountryCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("country_code is required");
        }
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z]{2}$")) {
            throw new IllegalArgumentException(
                    "country_code must be two lowercase letters (ISO alpha-2), e.g. 'it'");

        }
        return normalized;
    }

    // Собираем страницу фото с лайками, без N+1
    private PhotosPageResponse toProtoWithBatchedLikes(Slice<Photo> page, Integer total) {
        PhotosPageResponse.Builder builder = PhotosPageResponse.newBuilder()
                .setTotalElements(page.getNumberOfElements())
                .setFirst(page.isFirst())
                .setLast(page.isLast())
                .setPage(page.getNumber())
                .setSize(page.getSize());

        List<Photo> photos = page.getContent();
        List<UUID> photoIds = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            photoIds.add(photo.id());
        }

        Map<UUID, List<Like>> likesByPhoto = photoService.photoLikesMap(photoIds);

        for (Photo photo : photos) {
            PhotoResponse.Builder photoBuilder = PhotoResponse.newBuilder();
            photo.toProto(photoBuilder);

            List<Like> likes = likesByPhoto.getOrDefault(photo.id(), List.of());
            photoBuilder.setLikes(Like.toProtoList(likes));

            builder.addContent(photoBuilder.build());
        }

        if (total != null) {
            builder.setTotal(total);
        }

        return builder.build();
    }

    private static PhotoResponse toProto(Photo photo) {
        PhotoResponse.Builder b = PhotoResponse.newBuilder();
        photo.toProto(b);
        return b.build();
    }
}
