package guru.qa.rangiffler.service;

import guru.qa.rangiffler.entity.*;
import guru.qa.rangiffler.model.Like;
import guru.qa.rangiffler.model.Photo;
import guru.qa.rangiffler.model.input.PhotoInput;
import guru.qa.rangiffler.repository.*;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class PhotoService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final PhotoRepository photoRepository;
    private final StatisticRepository statisticRepository;
    private final LikeRepository likeRepository;


    @Autowired
    public PhotoService(UserRepository userRepository, CountryRepository countryRepository,
                        PhotoRepository photoRepository, StatisticRepository statisticRepository,
                        LikeRepository likeRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.photoRepository = photoRepository;
        this.statisticRepository = statisticRepository;
        this.likeRepository = likeRepository;
    }

    public Page<Photo> getPhotos(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return photoRepository.findAll(pageRequest)
                .map(Photo::fromPhotoEntity); // преобразуем PhotoEntity в Photo
    }


    @Transactional
    public Photo createPhoto(String username, PhotoInput input) {

        PhotoEntity photoEntity = new PhotoEntity();

        UserEntity userEntity = getUser(username);
        UUID userId = userEntity.getId();
        photoEntity.setUser(userEntity);

        if (input.src() != null) {
            if (input.src().startsWith("data:")) {
                String base64Data = input.src().substring(input.src().indexOf(",") + 1);
                photoEntity.setPhoto(Base64.getDecoder().decode(base64Data));
            } else {
                photoEntity.setPhoto(input.src().getBytes(StandardCharsets.UTF_8));

            }
        }
        CountryEntity countryEntity;
        if (input.country() != null) {

            countryEntity = countryRepository.findByCode(input.country().code()).orElseThrow(() -> new RuntimeException("Country not found"));
            //    UUID countryId = countryEntity.getId();
            photoEntity.setCountry(countryEntity);
        } else {
            countryEntity = null;
        }

        if (input.description() != null) {
            photoEntity.setDescription(input.description());
        }
        StatisticEntity statistic = statisticRepository.findByUserAndCountry(
                userEntity, countryEntity
        ).orElseGet(() -> {
            StatisticEntity se = new StatisticEntity();
            se.setUser(userEntity);
            se.setCount(0);
            se.setCountry(countryEntity);
            return se;
        });
        statistic.setCount(statistic.getCount() + 1);
        statisticRepository.save(statistic);


        photoEntity.setCreatedDate(new Date());


        PhotoEntity createdPhoto = photoRepository.save(photoEntity);
        return Photo.fromPhotoEntity(createdPhoto);

    }

    private UserEntity getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

//    @Transactional(readOnly = true)
//    public Slice<Photo> feedPhotos(@Nonnull String username,
//                                   @Nonnull Pageable pageable) {
//        UserEntity userEntity = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("Can`t find user by username: " + username));
//
//        //meAndFriends.add(userEntity);
//
//        return photoRepository.findByUserIdOrderByCreateionDateDesc(
//                userEntity.getId(), pageable
//        ).map(Photo::fromPhotoEntity);
//

    /// /        return photoRepository.findByUserInOrderByCreatedDateDesc(
    /// /                userEntity,
    /// /                pageable
    /// /        ).map(Photo::fromPhotoEntity);
//    }
    @Transactional
    public Photo addPhoto(@Nonnull String username,
                          @Nonnull PhotoInput photoInput) {
        UserEntity user = getRequiredUser(username);

        PhotoEntity photo = new PhotoEntity();
        CountryEntity country = countryRepository.findByCode(photoInput.country().code())
                .orElseThrow(() -> new NotFoundException("Country not found by code: " + photoInput.country().code()));
        //  photo.setPhoto(new StringAsBytes(photoInput.src()).bytes());
        photo.setDescription(photoInput.description());
        photo.setCreatedDate(new Date());
        photo.setCountry(country);
        photo.setUser(user);

        StatisticEntity statistic = statisticRepository.findByUserAndCountry(
                user, country
        ).orElseGet(() -> {
            StatisticEntity se = new StatisticEntity();
            se.setUser(user);
            se.setCount(0);
            se.setCountry(country);
            return se;
        });
        statistic.setCount(statistic.getCount() + 1);
        statisticRepository.save(statistic);
        return Photo.fromPhotoEntity(photoRepository.save(photo));
    }

//    @Transactional
//    public Photo editPhoto(@Nonnull String username, @Nonnull PhotoInput photoInput) {
//        PhotoEntity photo = getRequiredPhoto(photoInput.id());
//        if (photoInput.like() != null && !photoIsLikedBy(photo, photoInput.like().userId())) {
//            LikeEntity likeEntity = new LikeEntity();
//            likeEntity.setCreationDate(new Date());
//            likeEntity.setUser(getRequiredUser(photoInput.like().userId()));
//            photo.addLikes(likeEntity);
//        }
//        if (userHasFullAccessToPhoto(username, photo)) {
//            if (photoInput.country() != null) {
//                CountryEntity country = countryRepository.findByCode(photoInput.country().code())
//                        .orElseThrow(() -> new NotFoundException("Country not found by code: " + photoInput.country().code()));
//                photo.setCountry(country);
//            }
//            if (photoInput.description() != null) {
//                photo.setDescription(photoInput.description());
//            }
//            if (photoInput.src() != null) {
//                photo.setPhoto(new StringAsBytes(photoInput.src()).bytes());
//            }
//        }
//        return Photo.fromPhotoEntity(
//                photoRepository.save(photo)
//        );
//    }


    @Transactional
    public Photo updatePhoto(@Nonnull String username, @Nonnull PhotoInput photoInput) {
        UserEntity userEntity = getUser(username);
        PhotoEntity photoEntity = photoRepository.getReferenceById(photoInput.id());

        if (photoInput.like() == null) {

            if (photoInput.description() != null) {
                photoEntity.setDescription(photoInput.description());
            }
            if (photoInput.src() != null) {
                if (photoInput.src().startsWith("data:")) {
                    String base64Data = photoInput.src().substring(photoInput.src().indexOf(",") + 1);
                    photoEntity.setPhoto(Base64.getDecoder().decode(base64Data));
                } else {
                    photoEntity.setPhoto(photoInput.src().getBytes(StandardCharsets.UTF_8));

                }
            }

            if (photoInput.country() != null) {

                // уменьшили count старой страны на 1;
                CountryEntity oldCountryEntity = photoEntity.getCountry();
                StatisticEntity statisticEntity = statisticRepository.findByUserAndCountry(
                        userEntity, oldCountryEntity
                ).orElseThrow();
                int count = statisticEntity.getCount();

                statisticEntity.setCount(count - 1);
                statisticRepository.save(statisticEntity);

                if (statisticEntity.getCount() == 0) {
                    statisticRepository.deleteStatisticEntityById(statisticEntity.getId());
                }


                CountryEntity countryEntity = countryRepository.findByCode(photoInput.country().code()).orElseThrow();
                StatisticEntity statistic = statisticRepository.findByUserAndCountry(
                        userEntity, countryEntity
                ).orElseGet(() -> {
                    StatisticEntity se = new StatisticEntity();
                    se.setUser(userEntity);
                    se.setCount(0);
                    se.setCountry(countryEntity);
                    return se;
                });
                statistic.setCount(statistic.getCount() + 1);
                statisticRepository.save(statistic);
                photoEntity.setCountry(countryEntity);

            }


            PhotoEntity updatedPhoto = photoRepository.save(photoEntity);

            return Photo.fromPhotoEntity(updatedPhoto);
        } else {
            LikeEntity likeEntity = new LikeEntity();
            likeEntity.setUser(userEntity);
            likeEntity.setCreationDate(new Date());
            LikeEntity savedLike = likeRepository.save(likeEntity);

            photoEntity.getLikes().add(savedLike);
            photoRepository.save(photoEntity);

            return Photo.fromPhotoEntity(photoEntity);
        }
    }


    @Transactional(readOnly = true)
    public Slice<Photo> allUserPhotos(@Nonnull String username,
                                      @Nonnull Pageable pageable) {
        UserEntity userEntity = getRequiredUser(username);
        return photoRepository.findByUserOrderByCreatedDateDesc(
                userEntity,
                pageable
        ).map(Photo::fromPhotoEntity);
    }

    @Transactional(readOnly = true)
    public List<Like> photoLikes(@Nonnull UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Can`t find photo by id: " + photoId))
                .getLikes()
                .stream()
                .map(Like::fromEntity)
                .toList();
    }

//    @Transactional(readOnly = true)
//    public Slice<Photo> allFriendsPhotos(@Nonnull String username,
//                                            @Nonnull Pageable pageable) {
//        UserEntity userEntity = getRequiredUser(username);
//        return photoRepository.findByUserInOrderByCreatedDateDesc(
//                userRepository.findFriends(userEntity),
//                pageable
//        ).map(Photo::fromPhotoEntity);
//    }

    @Transactional(readOnly = true)
    public Slice<Photo> feedPhotos(@Nonnull String username,
                                   @Nonnull Pageable pageable) {
        UserEntity userEntity = getRequiredUser(username);
        List<UserEntity> meAndFriends = new ArrayList<>(
                   userRepository.findFriends(userEntity)
        );
        meAndFriends.add(userEntity);

        return photoRepository.findByUserInOrderByCreatedDateDesc(
                meAndFriends,
                pageable
        ).map(Photo::fromPhotoEntity);
    }

    @Transactional
    public Boolean deletePhoto(String username, UUID photoId) {
        UserEntity userEntity = getUser(username);
        PhotoEntity photoEntity = photoRepository.findById(photoId).orElseThrow();
        CountryEntity countryEntity = photoEntity.getCountry();

        StatisticEntity statisticEntity = statisticRepository.findByUserAndCountry(userEntity, countryEntity).orElseThrow();
        statisticEntity.setCount(statisticEntity.getCount() - 1);
        statisticRepository.save(statisticEntity);
        photoRepository.delete(photoEntity);
        return true;

    }

//    @Transactional
//    public Boolean deletePhoto(@Nonnull String username, @Nonnull UUID photoId) {
//        PhotoEntity photo = getRequiredPhoto(photoId);
//        UserEntity user = photo.getUser();
//        CountryEntity country = photo.getCountry();
//        if (userHasFullAccessToPhoto(username, photo)) {
//            StatisticEntity statistic = statisticRepository.findByUserAndCountry(
//                    user, country
//            ).orElseThrow(() -> new NotFoundException("Can`t find statistic by userid: " + user.getId() + " and countryId: " + country.getId()));
//            statistic.setCount(statistic.getCount() - 1);
//            statisticRepository.save(statistic);
//            photoRepository.delete(photo);
//        } else
//            throw new AccessDeniedException("Can`t access to photo");
//    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Can`t find user by username: " + username));
    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Can`t find user by id: " + userId));
    }

    @Nonnull
    PhotoEntity getRequiredPhoto(@Nonnull UUID photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new NotFoundException("Can`t find photo by id: " + photoId));
    }

    private boolean photoIsLikedBy(@Nonnull PhotoEntity photo, @Nonnull UUID user) {
        return photo.getLikes()
                .stream()
                .map(LikeEntity::getUser)
                .map(UserEntity::getId)
                .anyMatch(id -> id.equals(user));
    }

    private boolean userHasFullAccessToPhoto(@Nonnull String username, @Nonnull PhotoEntity photo) {
        return photo.getUser().getUsername().equals(username);
    }

}
