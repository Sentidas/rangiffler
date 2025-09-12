package ru.sentidas.rangiffler.data.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;

import java.util.Optional;
import java.util.UUID;

import static ru.sentidas.rangiffler.data.jpa.EntityManagers.em;

public class PhotoRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.photoJdbcUrl());

    public PhotoEntity create(PhotoEntity photo) {
        entityManager.joinTransaction();
        entityManager.persist(photo);
        return photo;
    }

    public PhotoEntity update(PhotoEntity photo) {
        entityManager.joinTransaction();
        entityManager.merge(photo);
        return photo;
    }


    public Optional<PhotoEntity> findById(UUID id) {
        return Optional.ofNullable(
                entityManager.find(PhotoEntity.class, id));
    }


    public Optional<PhotoEntity> findByUsernameAndDescription(String username, String description) {
        try {
            return Optional.of(
                    entityManager.createQuery("SELECT u FROM PhotoEntity u WHERE u.username= :username AND u.description= :description", PhotoEntity.class)
                            .setParameter("username", username)
                            .setParameter("description", description)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    public void remove(PhotoEntity photo) {
        entityManager.joinTransaction();

        PhotoEntity removablePhoto = entityManager.find(PhotoEntity.class, photo.getId());
        if(removablePhoto !=null) {
            entityManager.remove(removablePhoto);
        }
    }
}
