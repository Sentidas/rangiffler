package ru.sentidas.rangiffler.data.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LogEventEntity {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "event_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "photo_id", columnDefinition = "BINARY(16)")
    private UUID photoId;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "target_user_id", columnDefinition = "BINARY(16)")
    private UUID targetUserId;

    @Column(name = "source_service", nullable = false, length = 32)
    private String sourceService;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "trace_id", length = 128)
    private String traceId;

    @Column(name = "payload", nullable = false, columnDefinition = "JSON")
    @Convert(converter = ru.sentidas.rangiffler.utils.JsonNodeAttributeConverter.class)
    private JsonNode payload;

    @Column(name = "topic")
    private String topic;

    @Column(name = "partition_id")
    private Integer partition;

    @Column(name = "offset_value")
    private Long offset;
}
