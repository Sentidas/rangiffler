package guru.qa.rangiffler.repository;

import guru.qa.rangiffler.entity.CountryEntity;
import guru.qa.rangiffler.entity.StatisticEntity;
import guru.qa.rangiffler.entity.UserEntity;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatisticRepository extends JpaRepository<StatisticEntity, UUID> {

    @Nonnull
    List<StatisticEntity> findAllByUserIn(List<UserEntity> user);

    @Nonnull
    Optional<StatisticEntity> findByUserAndCountry(UserEntity user, CountryEntity country);

    UserEntity user(UserEntity user);

    void deleteStatisticEntityById(UUID id);


}