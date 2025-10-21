package ru.sentidas.rangiffler.test.gql.query;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetFriendsOfFriendsQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UsersApi;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.ErrorGql.*;

public class UserNestedQueriesTest extends BaseGraphQlTest {

    private final UsersDbClient apiClient = new UsersDbClient();
    private final UsersApi usersApi = new UsersApi(apolloClient);

    @Test
    @User(friends = 1)
    @ApiLogin
    @DisplayName("Вложенный запрос друзей -> друзей запрещён: TOO_MANY_SUB_QUERIES")
    void userMustFailWhenRequestingFriendsOfFriends(@Token String bearerToken, AppUser user) {

        final String friend = user.testData().friends().getFirst().username();
        apiClient.addFriends(new AppUser(friend, new TestData("12345")), 2);

        ApolloResponse<GetFriendsOfFriendsQuery.Data> friendsOfFriendsResponse = usersApi.getFriendsOfFriends(bearerToken);

        assertAll("too many sub-queries",
                () -> assertTrue(friendsOfFriendsResponse.hasErrors(), "response must contain errors"),
                () -> assertEquals("FORBIDDEN", classification(friendsOfFriendsResponse), "classification must be 'FORBIDDEN'"),
                () -> assertEquals("TOO_MANY_SUB_QUERIES", error(friendsOfFriendsResponse), "error must be 'TOO_MANY_SUB_QUERIES'"),
                () -> assertEquals("users", path(friendsOfFriendsResponse), "error path must be 'users'"),
                () -> assertEquals("Can't fetch over 1 friends sub-queries", message(friendsOfFriendsResponse), "message must be 'Can't fetch over 1 friends sub-queries'")
        );
    }
}
