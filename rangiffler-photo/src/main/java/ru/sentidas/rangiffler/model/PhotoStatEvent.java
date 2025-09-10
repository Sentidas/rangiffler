package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record PhotoStatEvent(
        UUID userId,
        String countryCode,
        int delta
) {}

