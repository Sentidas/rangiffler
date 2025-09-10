package ru.sentidas.rangiffler.model;

import java.util.Date;
import java.util.UUID;

public record Photo(
        UUID id,
        String src,
        String countryCode,
        String description,
        Date creationDate,
        Likes likes
) {
}
