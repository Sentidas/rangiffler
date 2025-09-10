package ru.sentidas.rangiffler.model.ggl.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CountryGqlInput(

        @JsonProperty("code")
        String code
) {

}
