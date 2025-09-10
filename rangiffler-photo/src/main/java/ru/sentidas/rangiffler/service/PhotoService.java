package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.data.entity.LikeEntity;
import ru.sentidas.rangiffler.data.entity.PhotoEntity;
import ru.sentidas.rangiffler.data.entity.PhotoLikeId;
import ru.sentidas.rangiffler.data.repository.LikeRepository;
import ru.sentidas.rangiffler.data.repository.PhotoRepository;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.CreatePhoto;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.PhotoStatEvent;
import ru.sentidas.rangiffler.utils.NotFoundException;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoService.class);

    private final PhotoRepository photoRepository;
    private final GrpcUserdataClient grpcUserdataClient;
    private final KafkaTemplate<String, PhotoStatEvent> kafkaTemplate;
    private final LikeRepository likeRepository;

    @Transactional
    public Photo addPhoto(@Nonnull CreatePhoto createPhoto) {
        PhotoEntity photo = new PhotoEntity();

        setPhoto(photo, createPhoto.src());
        photo.setDescription(createPhoto.description());
        photo.setCreatedDate(new Date());
        photo.setCountryCode(createPhoto.countryCode());
        photo.setUser(createPhoto.userId());
        PhotoEntity saved = photoRepository.save(photo);

        PhotoStatEvent photoStatEvent = new PhotoStatEvent(
                saved.getUser(),
                saved.getCountryCode(),
                +1
        );

        kafkaTemplate.send("rangiffler_photo", photoStatEvent);
        LOG.info("### Kafka topic [rangiffler_photo] sent message: {}", photoStatEvent);

        return Photo.fromEntity(saved);
    }

    @Transactional
    public Photo updatePhoto(@Nonnull Photo photo) {
        PhotoEntity photoEntity = getRequiredPhoto(photo.id());

        if (!userHasFullAccessToPhoto(photo.requesterId(), photoEntity)) {
            throw new ru.sentidas.rangiffler.ex.AccessDeniedException("Can`t access to photo");
        }

        if (photo.countryCode() != null) {
            photoEntity.setCountryCode(photo.countryCode());
        }
        if (photo.description() != null) {
            photoEntity.setDescription(photo.description());
        }
        setPhoto(photoEntity, photo.src());

        PhotoEntity updated = photoRepository.save(photoEntity);

        PhotoStatEvent photoStatEvent = new PhotoStatEvent(
                updated.getUser(),
                updated.getCountryCode(),
                +1
        );

        kafkaTemplate.send("rangiffler_photo", photoStatEvent);
        LOG.info("### Kafka topic [rangiffler_photo] sent message: {}", photoStatEvent);

        return Photo.fromEntity(
                photoRepository.save(updated)
        );
    }

    private void setPhoto(PhotoEntity photoEntity, String src) {
        if (src != null) {
            // Убедитесь, что это Data URL и извлеките base64 часть
            if (src.startsWith("data:")) {
                String base64Data = src.substring(src.indexOf(",") + 1);
                photoEntity.setPhoto(Base64.getDecoder().decode(base64Data));
            } else {
                // Если это не Data URL, сохраняем как есть (в байтах)
                photoEntity.setPhoto(src.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    public List<ru.sentidas.rangiffler.model.Like> photoLikes(@Nonnull UUID photoId) {
        return likeRepository.findAllByIdPhotoId(photoId)
                .stream()
                .map(ru.sentidas.rangiffler.model.Like::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Slice<Photo> userPhotos(@Nonnull String username,
                                   @Nonnull Pageable pageable) {
        UUID userId = getRequiredUser(username);

        return photoRepository.findByUserOrderByCreatedDateDesc(
                        userId,
                        pageable)
                .map(Photo::fromEntity);

    }

    @Transactional(readOnly = true)
    public Slice<Photo> feedPhotos(@Nonnull String username,
                                   @Nonnull Pageable pageable) {

        UUID userId = getRequiredUser(username);

        List<UUID> meAndFriends = new ArrayList<>(
                grpcUserdataClient.friendIdsAll(userId)
        );
        meAndFriends.add(userId);

        return photoRepository.findByUserInOrderByCreatedDateDesc(
                meAndFriends,
                pageable
        ).map(Photo::fromEntity);
    }

    @Transactional
    public void deletePhoto(@Nonnull UUID requesterId, @Nonnull UUID photoId) {
        PhotoEntity photoEntity = getRequiredPhoto(photoId);
        UUID userId = photoEntity.getUser();
        String countryCode = photoEntity.getCountryCode();


        if (userHasFullAccessToPhoto(requesterId, photoEntity)) {
            likeRepository.deleteByIdPhotoId(photoId);

            photoRepository.delete(photoEntity);



            PhotoStatEvent photoStatEvent = new PhotoStatEvent(
                    userId,
                    countryCode,
                    -1
            );

            kafkaTemplate.send("rangiffler_photo", photoStatEvent);
            LOG.info("### Kafka topic [rangiffler_photo] sent message: {}", photoStatEvent);


        } else
            throw new ru.sentidas.rangiffler.ex.AccessDeniedException("Can`t access to photo");
    }

    @Nonnull
    private UUID getRequiredUser(@Nonnull String username) {
        return grpcUserdataClient.getIdByUsername(username);
    }

    @Nonnull
    private String getRequiredUser(@Nonnull UUID userId) {
        return grpcUserdataClient.getUsernameById(userId);

    }

    @Nonnull
    PhotoEntity getRequiredPhoto(@Nonnull UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Can`t find photo by id: " + photoId));
    }

    @Transactional
    public Photo toggleLike(UUID requesterId, UUID photoId) {
        PhotoEntity photoEntity = getRequiredPhoto(photoId);

        if (userHasFullAccessToPhoto(requesterId, photoEntity)) {
            throw new ru.sentidas.rangiffler.ex.AccessDeniedException(
                    "Can`t access to like yourself"
            );
        }

        PhotoLikeId id = new PhotoLikeId();
        id.setUserId(requesterId);
        id.setPhotoId(photoId);

        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);
        }
        else {
            LikeEntity like = new LikeEntity();
            like.setId(id);
            like.setCreationDate(new Date());
            likeRepository.save(like);
        }

        return Photo.fromEntity(photoEntity);
    }

    private boolean userHasFullAccessToPhoto(@Nonnull UUID userId, @Nonnull PhotoEntity photo) {
        return photo.getUser().equals(userId);
    }

}
