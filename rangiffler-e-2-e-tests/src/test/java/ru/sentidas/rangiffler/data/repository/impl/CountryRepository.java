package ru.sentidas.rangiffler.data.repository.impl;

import jakarta.persistence.EntityManager;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.geo.CountryEntity;
import ru.sentidas.rangiffler.data.entity.photo.PhotoEntity;
import ru.sentidas.rangiffler.model.Country;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.sentidas.rangiffler.data.jpa.EntityManagers.em;

public class CountryRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.geoJdbcUrl());

    public List<String> findAllCodes() {
        return entityManager.createQuery("Select c.code FROM CountryEntity c", String.class)
                .getResultList();
    }

}