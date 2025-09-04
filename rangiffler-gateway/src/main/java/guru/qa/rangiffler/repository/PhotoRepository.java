package guru.qa.rangiffler.repository;

import guru.qa.rangiffler.entity.PhotoEntity;
import guru.qa.rangiffler.entity.UserEntity;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

    @Nonnull
    Slice<PhotoEntity> findByUserOrderByCreatedDateDesc(@Nonnull UserEntity user,
                                                        @Nonnull Pageable pageable);

    @Nonnull
    List<PhotoEntity> findByUserOrderByCreatedDateDesc(@Nonnull UserEntity user);

    @Nonnull
    Slice<PhotoEntity> findByUserInOrderByCreatedDateDesc(@Nonnull List<UserEntity> users,
                                                          @Nonnull Pageable pageable);
}
