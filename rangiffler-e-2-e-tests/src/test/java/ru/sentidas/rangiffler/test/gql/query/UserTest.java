package ru.sentidas.rangiffler.test.gql.query;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.rx2.Rx2Apollo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetUserQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UserApi;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.ErrorGql.*;

public class UserTest extends BaseGraphQlTest {

    private final UserApi userApi = new UserApi(apolloClient);

    @Test
    @User
    @ApiLogin
    @DisplayName("Корректные данные пользователя")
    void getUserReturnsCorrectFieldsWhenAuthorized(@Token String bearerToken, AppUser user) {
        GetUserQuery.Data response = userApi.getUser(bearerToken);

        assertAll("user fields",
                () -> assertEquals(user.id().toString(), response.user.id),
                () -> assertEquals(user.username(), response.user.username),
                () -> assertEquals(user.surname(), response.user.surname),
                () -> assertEquals(user.firstname(), response.user.firstname),
                () -> assertEquals(user.countryCode(), response.user.location.code),
                () -> assertNotNull(response.user.avatar, "avatar must be non-null")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Если страна не задана, по умолчанию возвращается код страны 'ru'")
    void getUserReturnsDefaultRuLocationWhenCountryNotSet(@Token String bearerToken) {
        GetUserQuery.Data response = userApi.getUser(bearerToken);
        assertEquals("ru", response.user.location.code);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("При наличии страны возвращаются поля локации: code и name")
    void getUserReturnsCountryFieldsWhenLocationPresent(@Token String bearerToken) {
        GetUserQuery.Data response = userApi.getUser(bearerToken);

        assertAll("location fields",
                () -> assertNotNull(response.user.location, "location must be non-null"),
                () -> assertNotNull(response.user.location.code, "country.code must be non-null"),
                () -> assertFalse(response.user.location.code.isEmpty(), "country.code must be non-empty"),
                () -> assertNotNull(response.user.location.name, "country.name must be non-null"),
                () -> assertFalse(response.user.location.name.isEmpty(), "country.name must be non-empty")
        );
    }

    @Test
    @User
    @DisplayName("Запрос данных пользователя без авторизации :FORBIDDEN")
    void getUserReturnsForbiddenWhenTokenMissing() {
        GetUserQuery query = GetUserQuery.builder().build();
        ApolloCall<GetUserQuery.Data> call = apolloClient.query(query);
        ApolloResponse<GetUserQuery.Data> response = Rx2Apollo.single(call).blockingGet();

        assertAll("FORBIDDEN on getUser without token",
                () -> assertTrue(response.hasErrors(), "response must contain errors"),
                () -> assertEquals("FORBIDDEN", classification(response), "classification must be FORBIDDEN"),
                () -> assertEquals("user", path(response), "error path must be 'user'"),
                () -> assertEquals("Access is denied", message(response), "message must be 'Access is denied'")
        );
    }
}
