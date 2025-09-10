package ru.sentidas.rangiffler.data.repository;


import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.sentidas.rangiffler.data.entity.PhotoEntity;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<PhotoEntity, UUID> {

    @Nonnull
    Slice<PhotoEntity> findByUserOrderByCreatedDateDesc(@Nonnull UUID userId,
                                                        @Nonnull Pageable pageable);


    @Nonnull
    Slice<PhotoEntity> findByUserInOrderByCreatedDateDesc(@Nonnull List<UUID> userIds,
                                                          @Nonnull Pageable pageable);
}
