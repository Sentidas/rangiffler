package ru.sentidas.rangiffler.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sentidas.rangiffler.data.entity.LogEventEntity;

import java.util.UUID;

public interface LogEventRepository extends JpaRepository<LogEventEntity, Long> {

    boolean existsByEventId(UUID eventId);
}