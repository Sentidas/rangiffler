package ru.sentidas.rangiffler.model.input;

import java.util.UUID;

public record PhotoInput(
        UUID id,
        String src,
        CountryGqlInput country,
        String description,
        LikeInput like
) {
}
