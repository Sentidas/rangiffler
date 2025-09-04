package guru.qa.rangiffler.model.input;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CountryInput(

        @JsonProperty("code")
        String code
) {

}
