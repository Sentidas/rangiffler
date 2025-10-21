package ru.sentidas.rangiffler.test.grpc.geo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.sentidas.rangiffler.grpc.CodeRequest;
import ru.sentidas.rangiffler.grpc.CountryResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Geo: getByCode")
public class GetCodeTest extends BaseTest {

    public static final String RU_FLAG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAeCAMAAABpA6zvAAAA+VBMVEVHcEyVFQ7S0tKXFg/R0dGYGBCcGRLU1NTW1takHhSeGxLW1tamHxWbFxfKysqhGxPU1NTR0dHT09PU1NTV1dXT09OSFA3///8AMp7MIRbPIxjKHhQAOacANaLRJRkAN6XTJxsAMJzWKx0APKjHHBMAQKsARa7X2NoAPqoCQqzWLiAALpnUKRyfGBCUFA4ISa61IBbm5ubMzMyuJBve3t4CKYgBI3YALJbDKR6uGBBJcbxUL2j5+ftskc7g4+luLFfr6+u+IBYAQqHt7vCpHhVDNnymudyMqdkoOI2JJkOnLTgAOZAAPpQsWrQAPZ1FIVa6yuf09PRBW5RdfvtPAAAAF3RSTlMAueiE0Cal/7jPTITtBwhlH2agL0JV2eqn9WIAAAGcSURBVDjLxdRbV4JAFIbhyfOx0jIlCNM0pBCPlYlCJB7LrP7/j2nvaSYH5aKuei9Zz3yzFhdDCHRcSOcTWD6dOz0m+x0WEoScpEMa1Me0OBZJoD/k6CSXD3W7JKFp9uSjyBt9TDrMhyIYmG683+8SrTMq7ofeppfYnQmADsCn4i/6R2j/YTEOcKTN5mNsPnu3g35CcfZJ4tp82GpdQ7qu1+v1hr4ejuein8zGrbt7Aoo7ZI3GFaYoV631EFvrpVKpAvD1eeu2TFHKSrl8gaFjcGeOsrLIKrcU+uc48zkO/c7Hvh3Cl2fm9pjgHhC+/bhARh2D4BbTqWVZ0+niQmB8DrrfAFw4qxtatWoYxmC1dKxFxTe3WXomsVYMVdFd0lRVRe/AJY6z7MlS7dEkvQF3BnMqdo5JkCzJEIfCnOp3EnV8cTsnMNExuDMnMokyBn9cIKOOQUMduG2W60oBrtYE2HZ7nudlklimiXkeHKjJMnPuY9M0D0jPy8SOoln2fmSjqaNwLNkUMk3zLBwlJJnKBrxK1McOoFg4FcUvX2nLmUOxnjYdAAAAAElFTkSuQmCC";
    public static final String FR_FLAG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAeCAMAAABpA6zvAAAA/1BMVEVHcEzLy8vLy8uioqLDJjQAGXECMY2vFiHAwsi8Ii6Xn7sAInrT09MCKYQGKYQAHHS0HiYAGnMDNI/BIy8AHHLLy8uvKChgKV2zGyeqEx20Gya+vr7U1NQDMpHMmqDPz8/Dw8MCMY3S0tK5gofMzMz////pHizqIS8AI5XrIzLsJTUAK5zoHCkANKPtKDgALZ4AKJruKzsAL6AAMJAAJZcAOKYAMqKtFB7nGibtJzYAIZIAG3i4GCPMGSQAH4+2weL3srcAM5jSIC0AIIIAJ5j4t721vt8AJoe/HyzFxcUAKo4AMaHW1tbRKDbn5+fv7+/4+Pjf39/epavOzs6/iIwTla5tAAAAJXRSTlMAZEIDycm8tYnd+dj+ri9gL+6D+AmvCR1VknIh1VHrINWbIpkFMBjMAwAAAY9JREFUOMtt1NdWAjEUheEgvSNFir0AQ5FBRukdFARkBvX9n8WckzIy4b/M+tZOrkIILXudvI/6fL5oIOA6e3ATpYuch5BYMmyIQrvdLhK5ylB/+yRQouBPrdfENzNmszfaM21StCxzfwAdidwEPR5PMEhRaj5fEwMVsnZ7UuRZprn//mJVGo2GhMxtJYTeX1jU9QRE1t62TkFwVQ7ZXKt1AgLrVTmUTlcgd9X5kELhdF2BwlU4ZHO6XjuGDekAbmZirlZTIDLqEBq2c0LpGJSu2fw4grYrU7gywNXQOWBVOoSbFp9rLpcqZK48Ami71yM4lUxAdEvqHLAiXWnUoVA6TVMhcwLitdSpkDuAi43tnLAsXB3gCh0wrX8Mbcchd/3+CVhCx6DtxgrkrosQn9cHp0DhOJTOAUvSARws+LXjk5A5hGNNGy8WAyx02JvWP1jnrjuERUTnYa/XGw59Qj879FPu5qNhp+Mn1ETjsTT/P9yXZ65A5g797xDq0IL5BCHeeFr9lNA/+qF8IZeFoz/d2Mxx0NYRVAAAAABJRU5ErkJggg==";
    public static final String FRANCE = "France";
    public static final String RUSSIAN_FEDERATION = "Russian Federation";
    private static final String INVALID_ARGUMENT_MESSAGE =
            "country_code must be two lowercase letters (ISO alpha-2), e.g. 'fr'";

    @Test
    @DisplayName("Возвращает корректно код/название страны/флаг страны при валидном коде страны")
    void getByCodeReturnsCountryWhenValidCodeProvided() {
        CodeRequest request = CodeRequest.newBuilder()
                .setCode("fr").build();

        CountryResponse response = geoBlockingStub.getByCode(request);

        assertAll("valid 'fr' response",
                () -> assertEquals("fr", response.getCode()),
                () -> assertEquals(FRANCE, response.getName()),
                () -> assertNotNull(response.getId(), "id must not be null"),
                () -> assertNotNull(response.getFlag(), "flag must not be null"),
                () -> assertEquals(FR_FLAG, response.getFlag())
        );
    }

    @Test
    @DisplayName("Нормализует регистр и возвращает код страны в верхнем регистре (RU → ru)")
    void getByCodeNormalizesCaseAndReturnsDataWhenUppercaseCodeProvided() {
        CodeRequest request = CodeRequest.newBuilder()
                .setCode("RU").build();

        CountryResponse response = geoBlockingStub.getByCode(request);

        assertAll("normalized 'ru' response",
                () -> assertNotNull(response.getId(), "id must not be null"),
                () -> assertEquals("ru", response.getCode()),
                () -> assertEquals(RUSSIAN_FEDERATION, response.getName()),
                () -> assertEquals(RU_FLAG, response.getFlag())
        );
    }

    // ==== Негативные сценарии: передача некорректного кода страны ====

    @Test
    @DisplayName("Возвращает INVALID_ARGUMENT при пустом коде")
    void getByCodeReturnsInvalidArgumentWhenCodeIsEmpty() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                geoBlockingStub.getByCode(
                        CodeRequest.newBuilder()
                                .setCode("")
                                .build()
                )
        );
        assertAll("invalid argument for empty code",
                () -> assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode()),
                () -> assertEquals(INVALID_ARGUMENT_MESSAGE, exception.getStatus().getDescription())
        );
    }

    @Test
    @DisplayName("Возвращает NOT_FOUND при неизвестной стране")
    void getByCodeReturnsNotFoundWhenCountryUnknown() {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                geoBlockingStub.getByCode(
                        CodeRequest.newBuilder()
                                .setCode("ww")
                                .build()
                )
        );
        assertAll("not found for unknown code",
                () -> assertEquals(Status.NOT_FOUND.getCode(), exception.getStatus().getCode()),
                () -> assertEquals("Country not found: ww", exception.getStatus().getDescription())
        );
    }


    @ValueSource(strings = {"FRA", "f", "1!"})
    @DisplayName("Возвращает INVALID_ARGUMENT при неверном формате кода")
    @ParameterizedTest(name = "[{index}] {0}")
    void getByCodeReturnsInvalidArgumentWhenCodeHasBadFormat(String code) {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                geoBlockingStub.getByCode(
                        CodeRequest.newBuilder()
                                .setCode(code)
                                .build()
                )
        );
        assertAll("invalid argument for bad format",
                () -> assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode()),
                () -> assertEquals(INVALID_ARGUMENT_MESSAGE, exception.getStatus().getDescription())
        );
    }
}
