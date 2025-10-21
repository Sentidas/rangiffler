package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record Country(
        UUID id,
        String code,
        String name,
        String flag
) {
}