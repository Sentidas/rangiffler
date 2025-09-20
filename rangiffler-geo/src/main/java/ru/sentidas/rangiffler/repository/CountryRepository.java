package ru.sentidas.rangiffler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sentidas.rangiffler.entity.CountryEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CountryRepository extends JpaRepository<CountryEntity, UUID> {


    Optional<CountryEntity> findByCode(String code);

    @Query("select c from CountryEntity c where c.code in :codes")
    List<CountryEntity> findAllByCodeIn(@Param("codes") List<String> codes);
}
