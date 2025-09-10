package ru.sentidas.rangiffler.model.ggl.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserGqlInput(

        @JsonProperty("firstname")
        String firstname,

        @JsonProperty("surname")
        String surname,

        @JsonProperty("avatar")
        String avatar,

        @JsonProperty("location")
        CountryGqlInput location
) {
}
