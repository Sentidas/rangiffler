package ru.sentidas.rangiffler.test.grpc.geo;

import com.google.protobuf.Empty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.CountriesResponse;
import ru.sentidas.rangiffler.grpc.CountryResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Grpc_Geo: allCountries")
public class AllCountriesTest extends BaseTest {

    private static final int EXPECTED_COUNTRY_COUNT = 238;
    private static final String ISO_CODE = "^[a-z]{2}$";

    @Test
    @DisplayName("Все страны: возвращается 238 уникальных стран с валидными полями")
    void allCountriesReturns238UniqueValidRecords() {
        CountriesResponse countriesResponse = geoBlockingStub.allCountries(Empty.getDefaultInstance());
        final Set<String> uniqueCodes = new HashSet<>();

        assertEquals(EXPECTED_COUNTRY_COUNT, countriesResponse.getCountriesCount());

        for (CountryResponse countryResponse : countriesResponse.getCountriesList()) {
            String code = countryResponse.getCode();

            assertAll("country must have valid fields",
                    () -> assertNotNull(countryResponse.getId(), "id must not be null"),
                    () -> assertNotNull(code, "code must not be null"),
                    () -> assertTrue(code.matches(ISO_CODE),
                            "code must be ISO alpha-2 in lower case, e.g. 'fr'"),
                    () -> assertNotNull(countryResponse.getName(), "name must not be null"),
                    () -> assertFalse(countryResponse.getName().isBlank(), "name must not be blank"),
                    () -> assertNotNull(countryResponse.getFlag(), "flag must not be null"),
                    () -> assertFalse(countryResponse.getFlag().isBlank(), "flag must not be blank"),
                    () -> assertTrue(uniqueCodes.add(code), "code must be unique")
            );
        }
    }
}
