package ru.sentidas.rangiffler.model.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PhotoStatEvent(
        UUID userId,
        String countryCode,
        int delta
) {}

