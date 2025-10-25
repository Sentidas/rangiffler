package ru.sentidas.rangiffler.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sentidas.rangiffler.entity.StatisticEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatisticsRepository extends JpaRepository<StatisticEntity, UUID> {


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

    @Modifying
    @Query(value = """
            INSERT INTO statistic(user_id, country_id, count) 
            VALUES (?1, ?2, 1)
            ON DUPLICATE KEY UPDATE count = count + 1
            """, nativeQuery = true)
    int upsertIncrement(UUID userId, UUID countryId);

    @Modifying
    @Query(value = """
            UPDATE statistic
            SET count = count - 1
            WHERE user_id = ?1 AND country_id = ?2 AND count > 1
            """, nativeQuery = true)
    int decreaseCountIfGreaterThanOne(UUID userId, UUID countryId);

    @Modifying
    @Query(value = """
            DELETE FROM statistic
            WHERE user_id = ?1 AND country_id = ?2 AND count = 1
            """, nativeQuery = true)
    int deleteIfCountEqualsOne(UUID userId, UUID countryId);
}
