package ru.sentidas.rangiffler.model;

import java.util.Optional;
import java.util.UUID;

public record UpdatePhoto(
        UUID id,
        Optional<String> src,
        Optional<String> countryCode,
        Optional<String> description

) {
}
