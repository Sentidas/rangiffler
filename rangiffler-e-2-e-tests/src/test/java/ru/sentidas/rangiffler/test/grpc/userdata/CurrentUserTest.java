package ru.sentidas.rangiffler.test.grpc.userdata;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.UpdateUserRequest;
import ru.sentidas.rangiffler.grpc.UserIdRequest;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.grpc.UsernameRequest;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL;

@GrpcTest
@DisplayName("Userdata: currentUser, currentUserById")
public class CurrentUserTest extends BaseTest {

    private static final String ISO_CODE = "^[a-z]{2}$";

    @Test
    @DisplayName("Корректно возвращаются обязательные данные после регистрации пользователя: id, username, code")
    @User
    void currentUserReturnsMinimalCardWhenUserHasNoOptionalFields(AppUser user) {
        UserResponse response = userdataBlockingStub.currentUser(
                UsernameRequest.newBuilder().setUsername(user.username()).build()
        );

        assertAll("Minimal user",
                () -> assertEquals(user.id().toString(), response.getId(), "id mismatch"),
                () -> assertEquals(user.username(), response.getUsername(), "username mismatch"),
                () -> assertNotNull(response.getCountryCode(), "country_code should not be null by default"),
                () -> assertEquals("ru", response.getCountryCode(), "country_code should be default 'ru'"),
                () -> assertTrue(response.getCountryCode().matches(ISO_CODE), "country_code should be ISO alpha-2 lowercase"),
                () -> assertFalse(response.hasAvatar(), "avatar should be absent"),
                () -> assertFalse(response.hasAvatarSmall(), "avatar_small should be absent"),
                () -> assertFalse(response.hasFirstname(), "firstname should be absent"),
                () -> assertFalse(response.hasSurname(), "surname should be absent"),
                () -> assertFalse(response.hasFriendStatus(), "friend_status should be absent")
        );
    }

    @Test
    @DisplayName("Возврат одинаковых данных пользователя при запросе по username и по ID")
    @User
    void currentUserByIdReturnsSameDataAsCurrentUserWhenQueriedById(AppUser user) {
        UserResponse byName = userdataBlockingStub.currentUser(
                UsernameRequest.newBuilder().setUsername(user.username()).build()
        );

        UserResponse byId = userdataBlockingStub.currentUserById(
                UserIdRequest.newBuilder().setUserId(user.id().toString()).build()
        );

        assertAll("Consistency between methods",
                () -> assertEquals(byName.getId(), byId.getId(), "id should be equal"),
                () -> assertEquals(byName.getUsername(), byId.getUsername(), "username should be equal"),
                () -> assertEquals(byName.getCountryCode(), byId.getCountryCode(), "country_code should be equal"),
                () -> assertEquals(byName.hasAvatar(), byId.hasAvatar(), "avatar presence should be equal")
        );
    }

    @Test
    @DisplayName("Корректно возвращаются все данные по пользователю при заполнении/изменении всех полей")
    @User
    void currentUserReturnsFullCardWhenAllFieldsAreSetInBlobMode(AppUser user) {
        userdataBlockingStub.updateUser(UpdateUserRequest.newBuilder()
                .setUsername(user.username())
                .setFirstname("Alina")
                .setSurname("Klimova")
                .setCountryCode("FR")
                .setAvatar(DATA_URL)
                .build()
        );

        UserResponse response = userdataBlockingStub.currentUser(
                UsernameRequest.newBuilder().setUsername(user.username()).build()
        );

        assertAll("Full user",
                () -> assertEquals(user.id().toString(), response.getId(), "id should match precondition"),
                () -> assertEquals(user.username(), response.getUsername(), "username should match precondition"),
                () -> assertTrue(response.hasFirstname(), "firstname should be present"),
                () -> assertEquals("Alina", response.getFirstname(), "firstname should match"),
                () -> assertTrue(response.hasSurname(), "surname should be present"),
                () -> assertEquals("Klimova", response.getSurname(), "surname should match"),
                () -> assertEquals("fr", response.getCountryCode(), "country_code should be normalized to lowercase"),
                () -> assertTrue(response.hasAvatar(), "avatar should be present")
        );
    }

    // ---------- Негативные сценарии ----------

    @Test
    @DisplayName("Текущий пользователь не существует_username: NOT_FOUND")
    void currentUserReturnsNotFoundWhenUsernameDoesNotExist() {
        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername("non_existing_user")
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> userdataBlockingStub.currentUser(request));

        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode(), "status should be NOT_FOUND");
    }

    @Test
    @DisplayName("Текущий пользователь не существует_id: NOT_FOUND")
    void currentUserByIdReturnsNotFoundWhenUserMissing() {
        final String unknownUserId = java.util.UUID.randomUUID().toString();
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(unknownUserId)
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> userdataBlockingStub.currentUserById(request));

        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode(), "status should be NOT_FOUND");
    }

    @Test
    @DisplayName("Неверный UUID user_id: INVALID_ARGUMENT")
    void currentUserByIdReturnsInvalidArgumentWhenUserIdIsNotUuid() {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId("not-a-uuid")
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> userdataBlockingStub.currentUserById(request));

        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(), "status should be INVALID_ARGUMENT");
    }
}

