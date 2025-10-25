package ru.sentidas.rangiffler.model.input;

public record UserGqlInput(

        String firstname,
        String surname,
        String avatar,
        CountryGqlInput location
) {
}
