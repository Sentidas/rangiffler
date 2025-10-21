package ru.sentidas.rangiffler.test.gql.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetFriendsQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UserApi;

import static org.junit.jupiter.api.Assertions.*;

public class FriendsTest extends BaseGraphQlTest {

    private final UserApi userApi = new UserApi(apolloClient);

    @Test
    @User(friends = 2)
    @ApiLogin
    @DisplayName("GetFriends: корректные поля друзей пользователя")
    void getFriendsMustReturnCorrectFields(@Token String bearerToken) {
        GetFriendsQuery.Data response = userApi.getFriends(bearerToken, 0, 10);

        assertAll("friends data",
                () -> assertNotNull(response.user.friends.edges, "edges must not be null"),
                () -> assertTrue(response.user.friends.edges.size() > 0, "friends must not be empty")
        );

        int index = 0;
        for (GetFriendsQuery.Edge edge : response.user.friends.edges) {
            GetFriendsQuery.Node friend = edge.node;
            int i = index++;
            assertAll("friend node #" + i,
                    () -> assertNotNull(friend.id, "id must not be null"),
                    () -> assertFalse(friend.id.isEmpty(), "id must not be empty"),
                    () -> assertNotNull(friend.username, "username must not be null"),
                    () -> assertNotNull(friend.friendStatus, "friendStatus must not be null")
            );
        }
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("GetFriends: пустой список, если друзей нет")
    void getFriendsMustReturnEmptyWhenNoFriends(@Token String bearerToken) {
        GetFriendsQuery.Data response = userApi.getFriends(bearerToken, 0, 10);
        assertEquals(0, response.user.friends.edges.size());
    }
}

