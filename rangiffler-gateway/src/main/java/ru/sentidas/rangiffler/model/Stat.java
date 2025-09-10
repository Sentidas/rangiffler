package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.model.ggl.input.Country;

public record Stat(
        int count,
        Country country
) {
}
