package ru.sentidas.rangiffler.data.repository;

import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;

import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository {

    PhotoEntity create(PhotoEntity photo);

    PhotoEntity update(PhotoEntity photo);

    Optional<PhotoEntity> findById(UUID id);

    Optional<PhotoEntity> findByUsernameAndDescription(String username, String description);

    Optional<PhotoEntity> findByUsernameAndCountry(String username, String code);

    void remove(PhotoEntity photo);
}
