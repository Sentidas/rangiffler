package ru.sentidas.rangiffler.data.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import ru.sentidas.rangiffler.data.jdbc.DataSources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManagers {

    private EntityManagers() {
    }

    private static final Map<String, EntityManagerFactory> emfs = new ConcurrentHashMap<>();

    public static EntityManager em(String jdbcUrl) {

        return new ThreadSafeEntityManager(
                emfs.computeIfAbsent(
                        jdbcUrl,
                        key -> {
                            DataSources.dataSource(jdbcUrl);
                            return Persistence.createEntityManagerFactory(jdbcUrl);
                        }
                ).createEntityManager()
        );
    }
    public static void closeAllEmfs() {
        emfs.values().forEach(EntityManagerFactory::close);
    }

}
