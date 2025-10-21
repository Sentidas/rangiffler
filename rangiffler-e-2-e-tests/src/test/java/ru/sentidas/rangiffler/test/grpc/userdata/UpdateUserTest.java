package ru.sentidas.rangiffler.test.grpc.userdata;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.UpdateUserRequest;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Userdata: updateUser")
public class UpdateUserTest extends BaseTest {

    @Test
    @DisplayName("Изменение данных пользователя: нормализует страну, когда приходит код страны с пробелами и разным регистром")
    @User
    void updateUserNormalizesCountryCodeWhenCodeHasSpacesAndMixedCase(AppUser user) {
        userdataBlockingStub.updateUser(UpdateUserRequest.newBuilder()
                .setUsername(user.username())
                .setCountryCode("Fr ")
                .build());

        final UserResponse userResponse = userdataBlockingStub.currentUser(
                ru.sentidas.rangiffler.grpc.UsernameRequest.newBuilder().setUsername(user.username()).build()
        );

        assertAll("Normalized country",
                () -> assertEquals("fr", userResponse.getCountryCode(), "country_code should be normalized to lowercase without spaces")
        );
    }

    // ==== Негативные сценарии =====

    @Test
    @DisplayName("Изменение данных пользователя_пустой username: INVALID_ARGUMENT")
    void updateUserRejectsInvalidArgumentWhenUsernameIsBlank() {
        final UpdateUserRequest request = UpdateUserRequest.newBuilder()
                .setUsername("")
                .build();

        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> userdataBlockingStub.updateUser(request));

        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(), "status should be INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("Изменение данных пользователя_код страны не ISO-2: INVALID_ARGUMENT")
    @User
    void updateUserRejectsInvalidArgumentWhenCountryCodeIsNotIso2(AppUser user) {
        final String[] badCodes = {"FRA", "1", "F", " ru3 "};

        for (final String code : badCodes) {
            final UpdateUserRequest request = UpdateUserRequest.newBuilder()
                    .setUsername(user.username())
                    .setCountryCode(code)
                    .build();

            final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                    () -> userdataBlockingStub.updateUser(request));

            assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(),
                    "status should be INVALID_ARGUMENT for code=" + code);
        }
    }
}
