package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record CreatePhoto (
        UUID userId,
        String src,
        String countryCode,
        String description
){
}
