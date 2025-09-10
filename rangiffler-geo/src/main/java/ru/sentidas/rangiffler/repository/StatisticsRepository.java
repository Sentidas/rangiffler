package ru.sentidas.rangiffler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sentidas.rangiffler.entity.StatisticEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<StatisticEntity, UUID> {

    Optional<Object> findByUserId(UUID uuid);

    Optional<StatisticEntity> findByUserIdAndCountryId(UUID userId, UUID countryId);

    @Query("""
           select s.countryId as countryId, sum(s.count) as total
           from StatisticEntity s
           where s.userId in :userIds
           group by s.countryId
           """)
    List<CountryStatRow> aggregateByUserIds(@Param("userIds") Collection<UUID> userIds);

    interface CountryStatRow {
        UUID getCountryId();
        Integer getTotal();
    }
}
