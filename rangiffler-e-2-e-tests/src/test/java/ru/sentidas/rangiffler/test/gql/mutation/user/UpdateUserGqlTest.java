
package ru.sentidas.rangiffler.test.gql.mutation.user;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetUserQuery;
import ru.sentidas.UpdateUserMutation;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UserApi;
import ru.sentidas.rangiffler.test.gql.support.ErrorGql;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL;

@DisplayName("GQL_Изменение профиля пользователя")
public class UpdateUserGqlTest extends BaseGraphQlTest {

    private final UserApi userApi = new UserApi(apolloClient);

    @Test
    @User
    @ApiLogin
    @DisplayName("Обновление профиля: меняются имя/фамилия/аватар/страна, id и username сохраняются")
    void updateUserUpdatesEditableFieldsAndKeepsIdAndUsernameWhenAuthorized(@Token String bearerToken) {
        GetUserQuery.Data userBefore = userApi.getUser(bearerToken);
        final String userIdBefore = userBefore.user.id;
        final String usernameBefore  = userBefore.user.username;

        final String newFirstname = "Ivan";
        final String newSurname = "Makarov";
        final String newCountryCode = "cn";

        UpdateUserMutation.Data updateResponse =
                userApi.updateUser(bearerToken, newFirstname, newSurname, DATA_URL, newCountryCode);

        assertAll("UPDATE response fields — editable fields changed, identifiers preserved",
                () -> assertEquals(userIdBefore, updateResponse.user.id, "id must remain the same"),
                () -> assertEquals(usernameBefore, updateResponse.user.username, "username must remain the same"),
                () -> assertEquals(newFirstname, updateResponse.user.firstname, "firstname must be updated"),
                () -> assertEquals(newSurname, updateResponse.user.surname, "surname must be updated"),
                () -> assertEquals(newCountryCode, updateResponse.user.location.code, "country code must be updated"),
                () -> assertNotNull(updateResponse.user.avatar, "avatar must be present"),
                () -> assertFalse(updateResponse.user.avatar.isBlank(), "avatar must be non-empty")
        );

        GetUserQuery.Data userAfter = userApi.getUser(bearerToken);

        assertAll("Postcondition — Query.user reflects updated fields and preserved identifiers",
                () -> assertEquals(updateResponse.user.id, userAfter.user.id, "id must match mutation response"),
                () -> assertEquals(updateResponse.user.username, userAfter.user.username, "username must match mutation response"),
                () -> assertEquals(newFirstname, userAfter.user.firstname, "firstname must be updated in Query.user"),
                () -> assertEquals(newSurname, userAfter.user.surname, "surname must be updated in Query.user"),
                () -> assertEquals(newCountryCode, userAfter.user.location.code, "country code must be updated in Query.user"),
                () -> assertFalse(userAfter.user.avatar.isBlank(), "avatar in Query.user must be non-empty")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Обновление профиля без авторизации: FORBIDDEN, данные не меняются")
    void updateUserFailsWithForbiddenAndKeepsDataWhenUnauthorized(@Token String bearerToken) {
        GetUserQuery.Data userBefore = userApi.getUser(bearerToken);

        ApolloResponse<UpdateUserMutation.Data> unauthorizedResponse =
                userApi.tryUpdateUserWithoutAuth("Marisa", "Black", null, "de");

        assertAll("FORBIDDEN on update without authorization",
                () -> assertTrue(unauthorizedResponse.hasErrors(), "response must contain errors"),
                () -> assertEquals("FORBIDDEN", ErrorGql.classification(unauthorizedResponse), "classification must be FORBIDDEN"),
                () -> assertEquals("user", ErrorGql.path(unauthorizedResponse), "error path must be 'user'"),
                () -> assertEquals("Access is denied", ErrorGql.message(unauthorizedResponse), "message must be 'Access is denied'")
        );

        GetUserQuery.Data userAfter = userApi.getUser(bearerToken);

        assertAll("Postcondition — Query.user remains unchanged after unauthorized attempt",
                () -> assertEquals(userBefore.user.id, userAfter.user.id, "id must remain unchanged"),
                () -> assertEquals(userBefore.user.username, userAfter.user.username, "username must remain unchanged"),
                () -> assertEquals(userBefore.user.firstname, userAfter.user.firstname, "firstname must remain unchanged"),
                () -> assertEquals(userBefore.user.surname, userAfter.user.surname, "surname must remain unchanged"),
                () -> assertEquals(userBefore.user.location.code, userAfter.user.location.code, "country code must remain unchanged"),
                () -> assertEquals(userBefore.user.avatar, userAfter.user.avatar, "avatar must remain unchanged")
        );
    }
}
