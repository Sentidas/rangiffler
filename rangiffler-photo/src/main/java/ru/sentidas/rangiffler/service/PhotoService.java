package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.EventType;
import ru.sentidas.rangiffler.data.entity.LikeEntity;
import ru.sentidas.rangiffler.data.entity.PhotoEntity;
import ru.sentidas.rangiffler.data.entity.PhotoLikeId;
import ru.sentidas.rangiffler.data.repository.LikeRepository;
import ru.sentidas.rangiffler.data.repository.PhotoRepository;
import ru.sentidas.rangiffler.ex.AccessDeniedException;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.grpc.client.GrpcUserdataClient;
import ru.sentidas.rangiffler.model.CreatePhoto;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.StorageType;

import java.util.*;
import java.util.stream.Collectors;

import static ru.sentidas.rangiffler.model.StorageType.BLOB;
import static ru.sentidas.rangiffler.model.StorageType.OBJECT;


@Service
@RequiredArgsConstructor
public class PhotoService {

    private static final Logger LOG = LoggerFactory.getLogger(PhotoService.class);

    private final PhotoRepository photoRepository;
    private final LikeRepository likeRepository;

    private final GrpcUserdataClient grpcUserdataClient;

    private final PhotoStatService photoStatService;
    private final ActivityService activityService;

    private final MinioService minioService;
    @Value("${app.media.storage.default:OBJECT}")
    private String storageMode;


    public StorageType storageMode() {
        return currentStorageMode();
    }

    @Transactional
    public Photo addPhoto(@Nonnull CreatePhoto createPhoto) {
        PhotoEntity photoEntity = new PhotoEntity();

        photoEntity.setDescription(createPhoto.description());
        photoEntity.setCreatedDate(new Date());
        photoEntity.setCountryCode(createPhoto.countryCode());
        photoEntity.setUser(createPhoto.userId());

        // парсим data: URL (фронт всегда отправляет dataURL при создании)
        DataUrl dataUrl = DataUrl.parse(createPhoto.src()); // бросит IllegalArgumentException, если не data:
        String mime = dataUrl.mime();
        byte[] bytes = dataUrl.bytes();

        StorageType mode = currentStorageMode(); // OBJECT или BLOB (настройка)

        if (mode == OBJECT) {
            try {
                // Пишем в MinIO; если MinIO недоступен — MinioService бросит StorageUnavailableException
                String key = minioService.upload(photoEntity.getUser(), bytes, mime);
                photoEntity.setPhotoUrl(key);
                photoEntity.setPhoto(null);
                photoEntity.setStorage(OBJECT);
                photoEntity.setPhotoMime(mime);
            } catch (ru.sentidas.rangiffler.ex.StorageUnavailableException sue) {
                // Пусть gRPC-Advice отдаст UNAVAILABLE с нормальным описанием
                throw sue;
            } catch (IllegalArgumentException iae) {
                // Неверный mime и т.п. — это ошибка клиента (INVALID_ARGUMENT в вашем Advice)
                throw iae;
            } catch (Exception e) {
                // Любая другая непредвиденная ошибка в OBJECT-ветке — это уже внутренняя
                throw new RuntimeException("Unexpected error while storing photo in OBJECT storage", e);
            }
        } else {
            // BLOB: пишем байты в БД (без внешних зависимостей)
            photoEntity.setPhoto(bytes);
            photoEntity.setPhotoUrl(null);
            photoEntity.setStorage(BLOB);
            photoEntity.setPhotoMime(mime);
        }

        // Сохраняем метаданные/контент
        PhotoEntity saved = photoRepository.save(photoEntity);
        Photo createdPhoto = Photo.fromEntity(saved);

        // Событие
        activityService.publishPhotoEvent(
                EventType.PHOTO_ADDED,
                createdPhoto.requesterId(),
                createdPhoto.id(),
                createdPhoto.requesterId(),
                createPhoto.countryCode()
        );

        // Дельта статистики
        photoStatService.sendDeltaAfterCommit(
                saved.getUser(),
                saved.getCountryCode(),
                +1
        );

        return createdPhoto;
    }

    @Transactional
    public Photo updatePhoto(@Nonnull Photo photo) {
        PhotoEntity photoEntity = getRequiredPhoto(photo.id());
        final String oldCountryCode = photoEntity.getCountryCode();

        if (!isOwner(photo.requesterId(), photoEntity)) {
            throw new AccessDeniedException("Cannot access to photo");
        }

        if (photo.countryCode() != null) {
            photoEntity.setCountryCode(photo.countryCode());
        }
        if (photo.description() != null) {
            photoEntity.setDescription(photo.description());
        }

        // Меняем картинку только если пришёл data:
        if (photo.src() != null && !photo.src().isBlank()) {
            if (photo.src().startsWith("data:")) {
                DataUrl parsed = DataUrl.parse(photo.src()); // IllegalArgumentException => INVALID_ARGUMENT
                String mime = parsed.mime();
                byte[] bytes = parsed.bytes();

                StorageType targetMode = currentStorageMode();
                StorageType previousMode = photoEntity.getStorage();

                if (targetMode == OBJECT) {
                    String oldKey = photoEntity.getPhotoUrl();
                    try {
                        String newKey = minioService.upload(photoEntity.getUser(), bytes, mime); // может бросить StorageUnavailableException
                        photoEntity.setPhotoUrl(newKey);
                        photoEntity.setPhoto(null);
                        photoEntity.setStorage(OBJECT);
                        photoEntity.setPhotoMime(mime);

                        // Если ключ поменяли — старый удаляем best-effort
                        if (oldKey != null && !oldKey.isBlank() && !oldKey.equals(newKey)) {
                            minioService.deleteObject(oldKey);
                        }
                    } catch (ru.sentidas.rangiffler.ex.StorageUnavailableException sue) {
                        // Отдаём наверх как UNAVAILABLE
                        throw sue;
                    } catch (IllegalArgumentException iae) {
                        // Неподдерживаемый mime и т.п. — ошибка клиента
                        throw iae;
                    } catch (Exception e) {
                        // Любая иная непредвиденная ошибка в OBJECT-ветке
                        throw new RuntimeException("Unexpected error while updating photo content in OBJECT storage", e);
                    }
                } else {
                    // Переезд/обновление в BLOB: если раньше было OBJECT — чистим объект best-effort
                    if (previousMode == OBJECT) {
                        String ok = photoEntity.getPhotoUrl();
                        if (ok != null && !ok.isBlank()) {
                            minioService.deleteObject(ok);
                        }
                    }
                    photoEntity.setPhoto(bytes);
                    photoEntity.setPhotoUrl(null);
                    photoEntity.setStorage(BLOB);
                    photoEntity.setPhotoMime(mime);
                }
            } else {
                // echo-URL — не меняем контент
                LOG.debug("UpdatePhoto: non data: src received -> treating as 'no image change'");
            }
        }

        PhotoEntity updated = photoRepository.save(photoEntity);
        Photo updatedPhoto = Photo.fromEntity(updated);

        activityService.publishPhotoEvent(
                EventType.PHOTO_UPDATED,
                updatedPhoto.requesterId(),
                updatedPhoto.id(),
                updatedPhoto.requesterId(),
                photo.countryCode()
        );

        if (photo.countryCode() != null && !photo.countryCode().isBlank()) {
            photoStatService.sendDeltaAfterCommit(updated.getUser(), updated.getCountryCode(), +1);
            photoStatService.sendDeltaAfterCommit(updated.getUser(), oldCountryCode, -1);
        }

        return updatedPhoto;
    }

    public List<Like> photoLikes(@Nonnull UUID photoId) {
        return likeRepository.findAllByIdPhotoId(photoId)
                .stream()
                .map(Like::fromEntity)
                .toList();
    }

    // батч-метод для листингов (убирает N+1 на странице)
    @Transactional(readOnly = true)
    public Map<UUID, List<Like>> photoLikesMap(Collection<UUID> photoIds) {
        if (photoIds == null || photoIds.isEmpty()) {
            return Map.of();
        }

        // Один запрос на все фото страницы, с упорядочением: в каждом фото новые лайки идут первыми
        List<LikeEntity> entities = likeRepository.findAllByPhotoIds(photoIds);

        // Выставляем пустые списки в порядке входных идентификаторов — чтобы сохранить порядок фото
        Map<UUID, List<Like>> byPhoto = new LinkedHashMap<>();
        for (UUID id : photoIds) {
            byPhoto.put(id, new ArrayList<>());
        }

        for (LikeEntity entity : entities) {
            UUID pid = entity.getId().getPhotoId();
            Like like = ru.sentidas.rangiffler.model.Like.fromEntity(entity);
            List<Like> list = byPhoto.computeIfAbsent(pid, k -> new ArrayList<>());
            list.add(like);
        }

        return byPhoto.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> List.copyOf(e.getValue()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    //   ==== Листинги фото ====

    @Transactional(readOnly = true)
    public Slice<Photo> userPhotos(@Nonnull UUID userId,
                                   @Nonnull Pageable pageable) {

        return photoRepository.findByUserOrderByCreatedDateDesc(
                        userId,
                        pageable)
                .map(Photo::fromEntity);

    }

    @Transactional(readOnly = true)
    public Slice<Photo> feedPhotos(@Nonnull UUID userId,
                                   @Nonnull Pageable pageable) {

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
        UUID ownerId = photoEntity.getUser();
        String countryCode = photoEntity.getCountryCode();

        if (!isOwner(requesterId, photoEntity)) {
            throw new ru.sentidas.rangiffler.ex.AccessDeniedException("Cannot access to photo");
        }

        // чистим лайки
        likeRepository.deleteByIdPhotoId(photoId);

        // если фото хранилось в OBJECT — удаляем файл из MinIO (best-effort)
        if (photoEntity.getStorage() == OBJECT) {
            String key = photoEntity.getPhotoUrl();
            if (key != null && !key.isBlank()) {
                minioService.deleteObject(key);
            }
        }

        // удаляем запись фото
        photoRepository.delete(photoEntity);

        // статистика: delta -1
        photoStatService.sendDeltaAfterCommit(ownerId, countryCode, -1);

        // событие: удаление фото
        activityService.publishPhotoEvent(
                ru.sentidas.rangiffler.EventType.PHOTO_DELETED,
                requesterId,  // инициатор
                photoId,
                ownerId,      // владелец
                countryCode
        );
    }

    @Transactional
    public Photo toggleLike(UUID requesterId, UUID photoId) {
        PhotoEntity photoEntity = getRequiredPhoto(photoId);

        if (isOwner(requesterId, photoEntity)) {
            throw new ru.sentidas.rangiffler.ex.AccessDeniedException("Self-like is not allowed");
        }

        PhotoLikeId id = new PhotoLikeId();
        id.setUserId(requesterId);
        id.setPhotoId(photoId);

        Photo likedPhoto = Photo.fromEntity(photoEntity);

        if (likeRepository.existsById(id)) {
            likeRepository.deleteById(id);

            activityService.publishPhotoEvent(
                    ru.sentidas.rangiffler.EventType.LIKE_REMOVED,
                    requesterId,
                    likedPhoto.id(),
                    likedPhoto.requesterId(),
                    likedPhoto.countryCode()
            );

        } else {
            LikeEntity like = new LikeEntity();
            like.setId(id);
            like.setCreationDate(new Date());
            likeRepository.save(like);

            activityService.publishPhotoEvent(
                    ru.sentidas.rangiffler.EventType.LIKE_ADDED,
                    requesterId,
                    likedPhoto.id(),
                    likedPhoto.requesterId(),
                    likedPhoto.countryCode()
            );
        }

        return likedPhoto;
    }

    //   ==== Счетчики ====

    @Transactional(readOnly = true)
    public int countUserPhotos(UUID userId) {
        return photoRepository.countByUser(userId);
    }

    @Transactional(readOnly = true)
    public int countFeedPhotos(UUID userId) {
        List<UUID> meAndFriends = new ArrayList<>(grpcUserdataClient.friendIdsAll(userId));
        meAndFriends.add(userId);
        return photoRepository.countByUserIn(meAndFriends);
    }

    //   ==== Helpers ====

    private boolean isOwner(@Nonnull UUID userId, @Nonnull PhotoEntity photo) {
        return photo.getUser().equals(userId);
    }

    @Nonnull
    PhotoEntity getRequiredPhoto(@Nonnull UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Cannot find photo by id: " + photoId));
    }

    private StorageType currentStorageMode() {
        try {
            return StorageType.valueOf(storageMode.toUpperCase());
        } catch (Exception e) {
            LOG.warn("Unknown storage mode '{}', fallback to BLOB", storageMode);
            return BLOB;
        }
    }
}
