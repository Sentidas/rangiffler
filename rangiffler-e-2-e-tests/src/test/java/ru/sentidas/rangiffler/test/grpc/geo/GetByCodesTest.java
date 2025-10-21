package ru.sentidas.rangiffler.test.grpc.geo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sentidas.rangiffler.grpc.CodesRequest;
import ru.sentidas.rangiffler.grpc.CountriesResponse;
import ru.sentidas.rangiffler.grpc.CountryResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Grpc_Geo: getByCodes")
public class GetByCodesTest extends BaseTest {

    private static final String INVALID_ARGUMENT_MESSAGE =
            "country_code must be two lowercase letters (ISO alpha-2), e.g. 'fr'";

    @Test
    @DisplayName("Порядок кодов страны сохраняется, регистр нормализуется")
    void getByCodesPreservesOrderAndNormalizesCaseWhenMixedCaseProvided() {
        final List<String> lowercaseInput = Arrays.asList("fr", "it", "es");
        final List<String> mixedCaseInput = Arrays.asList("FR", "It", "eS", "cn");

        CountriesResponse lowercaseResponse = geoBlockingStub.getByCodes(
                CodesRequest.newBuilder().addAllCodes(lowercaseInput).build()
        );
        CountriesResponse mixedCaseResponse = geoBlockingStub.getByCodes(
                CodesRequest.newBuilder().addAllCodes(mixedCaseInput).build()
        );

        final List<String> lowercaseCodes = codes(lowercaseResponse);
        final List<String> mixedCaseCodes = codes(mixedCaseResponse);

        assertAll("order and normalization",
                () -> assertEquals(Arrays.asList("fr", "it", "es"), lowercaseCodes),
                () -> assertEquals(Arrays.asList("fr", "it", "es", "cn"), mixedCaseCodes)
        );
    }

    @Test
    @DisplayName("Отсутствующие коды страны игнорируются, порядок сохраняется")
    void getByCodesIgnoresUnknownCodesAndPreservesOrderWhenInputContainsUnknownAndSpaces() {
        final List<String> mixedCaseInput = Arrays.asList("FR", " fr", "ww", "It", "eS", "cn", "QQ");

        CountriesResponse response = geoBlockingStub.getByCodes(
                CodesRequest.newBuilder().addAllCodes(mixedCaseInput).build()
        );

        assertEquals(Arrays.asList("fr", "fr", "it", "es", "cn"), codes(response));
    }

    @Test
    @DisplayName("Дубликаты кодов страны во входе → дубликаты в ответе")
    void getByCodesPreservesDuplicatesWhenInputContainsDuplicates() {
        final List<String> mixedCaseInput = Arrays.asList("fr", "fr", "ww", "FR", "eS", "cn", "es");

        CountriesResponse response = geoBlockingStub.getByCodes(
                CodesRequest.newBuilder().addAllCodes(mixedCaseInput).build()
        );

        assertEquals(Arrays.asList("fr", "fr", "fr", "es", "cn", "es"), codes(response));
    }

    @Test
    @DisplayName("Пустой список кодов стран → пустой ответ")
    void getByCodesReturnsEmptyResponseWhenInputIsEmpty() {
        CountriesResponse response = geoBlockingStub.getByCodes(
                CodesRequest.newBuilder().build()
        );

        assertEquals(0, response.getCountriesCount());
    }

    // ==== Негативные сценарии: передача некорректного кода страны ====

    @Test
    @DisplayName("Возвращает INVALID_ARGUMENT при пустом элементе в списке кодов стран")
    void getByCodesReturnsInvalidArgumentWhenInputContainsEmptyElement() {
        final List<String> inputCodes = Arrays.asList("fr", "");

        io.grpc.StatusRuntimeException exception = assertThrows(
                io.grpc.StatusRuntimeException.class,
                () -> geoBlockingStub.getByCodes(
                        CodesRequest.newBuilder().addAllCodes(inputCodes).build()
                )
        );
        assertAll("invalid argument for empty element format",
                () -> assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode()),
                () -> assertEquals(INVALID_ARGUMENT_MESSAGE, exception.getStatus().getDescription())
        );
    }

    private static Stream<List<String>> invalidCodesProvider() {
        return Stream.of(
                Arrays.asList("FRA", "it"),
                Arrays.asList("1!", "cn"),
                Arrays.asList("123", "it")
        );
    }

    @ParameterizedTest(name = "invalid format {0}")
    @MethodSource("invalidCodesProvider")
    @DisplayName("Возвращает INVALID_ARGUMENT при неверном формате элемента в списке кодов стран")
    void getByCodesReturnsInvalidArgumentWhenInputContainsBadFormat(List<String> inputCodes) {
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () ->
                geoBlockingStub.getByCodes(
                        CodesRequest.newBuilder()
                                .addAllCodes(inputCodes)
                                .build()
                )
        );
        assertAll("invalid argument for bad element format",
                () -> assertEquals(Status.INVALID_ARGUMENT.getCode(), exception.getStatus().getCode()),
                () -> assertEquals(INVALID_ARGUMENT_MESSAGE, exception.getStatus().getDescription())
        );
    }

    @NotNull
    private static List<String> codes(CountriesResponse response) {
     return response.getCountriesList().stream()
             .map(CountryResponse::getCode).collect(Collectors.toList());
    }
}
