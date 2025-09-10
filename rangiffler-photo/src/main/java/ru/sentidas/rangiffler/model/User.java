package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record User(
        UUID id,
        String username
) {
}
