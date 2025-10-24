package ru.sentidas.rangiffler.data.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.geo.CountryEntity;
import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;

import java.util.List;
import java.util.Optional;

import static ru.sentidas.rangiffler.data.jpa.EntityManagers.em;

public class CountryRepositoryImpl  {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.geoJdbcUrl());

    public static CountryRepositoryImpl getInstance() {
        return new CountryRepositoryImpl();
    }

    public List<String> findAllCodes() {
        return entityManager.createQuery("Select c.code FROM CountryEntity c", String.class)
                .getResultList();
    }

    public Optional<CountryEntity> findByCode(String code) {
        try {
            return Optional.of(
                    entityManager.createQuery("SELECT c FROM CountryEntity c WHERE c.code = :code", CountryEntity.class)
                            .setParameter("code", code)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}