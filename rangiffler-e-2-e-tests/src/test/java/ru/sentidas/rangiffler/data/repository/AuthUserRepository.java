package ru.sentidas.rangiffler.data.repository;

import ru.sentidas.rangiffler.data.entity.auth.AuthUserEntity;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository {
    AuthUserEntity create(AuthUserEntity user);

    AuthUserEntity update(AuthUserEntity user);

    Optional<AuthUserEntity> findById(UUID id);

    Optional<AuthUserEntity> findByUsername(String username);

    void remove(AuthUserEntity user);
}
