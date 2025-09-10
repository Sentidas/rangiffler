package ru.sentidas.rangiffler.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sentidas.rangiffler.data.entity.LikeEntity;
import ru.sentidas.rangiffler.data.entity.PhotoLikeId;

import java.util.List;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<LikeEntity, PhotoLikeId> {

    boolean existsById(PhotoLikeId id);

    void deleteById(PhotoLikeId id);

    long countByIdPhotoId(UUID photoId);

    List<LikeEntity> findAllByIdPhotoId(UUID photoId);

    void deleteByIdPhotoId(UUID photoId);

}
