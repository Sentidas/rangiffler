package ru.sentidas.rangiffler.model.kafka;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityEvent(
        UUID eventId,            // для идемпотентности в БД логов
        EventType eventType,     // тип события
        Instant occurredAt,      // реальное время события (UTC)
        UUID userId,             // «кто» сделал (actor)
        UUID photoId,            // фото (если релевантно)
        UUID targetUserId,       // «над кем» действие (владелец фото, адресат дружбы)
        String sourceService,    // auth | photo | userdata | geo
        Map<String, Object> payload // свободные атрибуты (countryCode, description, ...)
) {
    @JsonCreator
    public ActivityEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("userId") UUID userId,
            @JsonProperty("photoId") UUID photoId,
            @JsonProperty("targetUserId") UUID targetUserId,
            @JsonProperty("sourceService") String sourceService,
            @JsonProperty("payload") Map<String, Object> payload
    ) {
        this.eventId = eventId;
        this.eventType = eventType == null ? EventType.UNKNOWN : eventType;
        this.occurredAt = occurredAt;
        this.userId = userId;
        this.photoId = photoId;
        this.targetUserId = targetUserId;
        this.sourceService = sourceService;
        this.payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
