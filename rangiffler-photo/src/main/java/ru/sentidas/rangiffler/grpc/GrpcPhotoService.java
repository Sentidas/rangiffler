package ru.sentidas.rangiffler.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.CreatePhoto;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.service.PhotoService;

import java.util.List;
import java.util.UUID;


@GrpcService
public class GrpcPhotoService extends RangifflerPhotoServiceGrpc.RangifflerPhotoServiceImplBase {


    private final PhotoService photoService;
    private final GrpcUserdataClient grpcUserdataClient;

    @Autowired
    public GrpcPhotoService(PhotoService photoService, GrpcUserdataClient grpcUserdataClient) {
        this.photoService = photoService;
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @Transactional
    @Override
    public void createPhoto(CreatePhotoRequest request, StreamObserver<PhotoResponse> responseObserver) {
        CreatePhoto createdPhoto = new CreatePhoto(
                UUID.fromString(request.getUserId()),
                request.getSrc(),
                request.getCountryCode(),
                request.getDescription()
        );
        Photo photo = photoService.addPhoto(createdPhoto);
        responseObserver.onNext(toProto(photo));
        responseObserver.onCompleted();
    }

    @Transactional
    public void updatePhoto(UpdatePhotoRequest request, StreamObserver<PhotoResponse> responseObserver) {
        Photo photo = Photo.fromProto(request);
        Photo updated = photoService.updatePhoto(photo);
        responseObserver.onNext(toProto(updated));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    public void getUserPhotos(PhotoPageRequest request, StreamObserver<PhotosPageResponse> responseObserver) {
        final String username = grpcUserdataClient.getUsernameById(UUID.fromString(request.getUserId()));
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<Photo> photos = photoService.userPhotos(username, pageable);
        responseObserver.onNext(toProto(photos));
        responseObserver.onCompleted();

    }
    @Transactional(readOnly = true)
    public void getFeedPhotos(PhotoPageRequest request, StreamObserver<PhotosPageResponse> responseObserver) {
        final String username = grpcUserdataClient.getUsernameById(UUID.fromString(request.getUserId()));
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<Photo> photos = photoService.feedPhotos(username, pageable);
        responseObserver.onNext(toProto(photos));
        responseObserver.onCompleted();

    }

    @Transactional
    @Override
    public void deletePhoto(DeletePhotoRequest request, StreamObserver<Empty> responseObserver) {
        photoService.deletePhoto(
                UUID.fromString(request.getRequesterId()),
                UUID.fromString(request.getId())
        );
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

//    @Transactional
//    @Override
//    public void toggleLike(LikeRequest request, StreamObserver<PhotoResponse> responseObserver) {
//        UUID user = UUID.fromString(request.getUserId());
//        UUID photoId = UUID.fromString(request.getPhotoId());
//
//        Photo updated = photoService.toggleLike(user, photoId);
//        responseObserver.onNext(toProto(updated));
//        responseObserver.onCompleted();
//    }

    @Transactional
    @Override
    public void toggleLike(LikeRequest request, StreamObserver<PhotoResponse> out) {
        UUID requesterId = UUID.fromString(request.getUserId());
        UUID photoId     = UUID.fromString(request.getPhotoId());

        // переключили лайк
        Photo photo = photoService.toggleLike(requesterId, photoId);

        // добрали актуальные лайки
        var likes = photoService.photoLikes(photoId);

        // Гарантируем не-null список
        if (likes == null) {
            likes = List.of();
        }

        // List<ru.sentidas.rangiffler.model.Like>
       // likes = likes != null ? likes : null;
        // Гарантируем не-null список
//        var likes = java.util.Optional.ofNullable(photoService.photoLikes(photoId))
//                .orElseGet(java.util.List::of);// List<ru.sentidas.rangiffler.model.Like>
//        // likes = likes != null ? likes : null;

        // собрали ответ
        PhotoResponse.Builder b = PhotoResponse.newBuilder();
        photo.toProto(b);                             // id/src/country/description/creationDate
        b.setLikes(ru.sentidas.rangiffler.model.Like.toProtoList(likes)); // total + список

        out.onNext(b.build());
        out.onCompleted();
    }


    private static PhotosPageResponse toProto(Slice<Photo> page) {
        PhotosPageResponse.Builder b = PhotosPageResponse.newBuilder();
        b.setTotalElements(page.getNumberOfElements());
        b.setTotalPages(page.getPageable().getPageSize()); // вместо page.getPageable().getPageSize()
        b.setFirst(page.isFirst());
        b.setLast(page.isLast());
        b.setPage(page.getNumber());
        b.setSize(page.getSize());
        for (Photo p : page.getContent()) {
            b.addContent(toProto(p));
        }
        return b.build();
    }

    private static PhotoResponse toProto(Photo photo) {
        PhotoResponse.Builder b = PhotoResponse.newBuilder();
        photo.toProto(b);
        return b.build();
    }
}
