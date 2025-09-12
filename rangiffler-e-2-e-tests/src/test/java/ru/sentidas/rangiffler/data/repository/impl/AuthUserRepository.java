package ru.sentidas.rangiffler.data.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.auth.AuthUserEntity;

import java.util.Optional;
import java.util.UUID;

import static ru.sentidas.rangiffler.data.jpa.EntityManagers.em;


public class AuthUserRepository implements ru.sentidas.rangiffler.data.repository.AuthUserRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.authJdbcUrl());

    @Override
    public AuthUserEntity create(AuthUserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        return user;
    }

    @Override
    public AuthUserEntity update(AuthUserEntity user) {
        entityManager.joinTransaction();
        entityManager.merge(user);
        return user;
    }

    @Override
    public Optional<AuthUserEntity> findById(UUID id) {
        return Optional.ofNullable(
                entityManager.find(AuthUserEntity.class, id));
    }

    @Override
    public Optional<AuthUserEntity> findByUsername(String username) {
        try {
            return Optional.of(
                    entityManager.createQuery("SELECT u FROM AuthUserEntity u WHERE u.username=: username", AuthUserEntity.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public void remove(AuthUserEntity user) {
        entityManager.joinTransaction();
        AuthUserEntity removableUser = entityManager.find(AuthUserEntity.class, user.getId());
        if (removableUser != null) {
            entityManager.remove(removableUser);
        }
    }
}
