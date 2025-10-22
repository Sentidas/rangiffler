package ru.sentidas.rangiffler.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sentidas.rangiffler.data.entity.LikeEntity;
import ru.sentidas.rangiffler.data.entity.PhotoLikeId;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<LikeEntity, PhotoLikeId> {

    // Батч-получение всех лайков для набора
    // Заменяет многократные вызовы findAllByIdPhotoId(...) на один запрос для страницы
    @Query("""
        select l
        from LikeEntity l
        where l.id.photoId in :photoIds
        order by l.id.photoId asc, l.creationDate desc
    """)
    List<LikeEntity> findAllByPhotoIds(@Param("photoIds") Collection<UUID> photoIds);

    boolean existsById(PhotoLikeId id);

    void deleteById(PhotoLikeId id);

    List<LikeEntity> findAllByIdPhotoId(UUID photoId);

    void deleteByIdPhotoId(UUID photoId);
}
