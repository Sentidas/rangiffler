package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record UserEvent(
        UUID id,
        String username
) {
}
