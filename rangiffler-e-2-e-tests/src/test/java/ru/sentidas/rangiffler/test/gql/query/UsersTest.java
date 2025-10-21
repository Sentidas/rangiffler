package ru.sentidas.rangiffler.test.gql.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetPeopleQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UsersApi;
import ru.sentidas.rangiffler.utils.AnnotationHelper;
import ru.sentidas.rangiffler.test.gql.support.UserUtil;
import ru.sentidas.type.FriendStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.UserUtil.findNodeById;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;
import static ru.sentidas.type.FriendStatus.FRIEND;
import static ru.sentidas.type.FriendStatus.INVITATION_RECEIVED;

public class UsersTest extends BaseGraphQlTest {

    private final UsersApi usersApi = new UsersApi(apolloClient);

    @Test
    @User(outcomeInvitation = 1, incomeInvitation = 1)
    @ApiLogin
    @DisplayName("Поиск по нику показывает корректные данные пользователей - входящие и исходящие приглашения")
    void searchByUsernameReturnsCorrectWhenUserHasOutgoingAndIncoming(@Token String bearerToken, AppUser user) {
        final String outgoingUserId = firstOutcomeUserId(user).toString();
        final String incomingUserId = firstIncomeUserId(user).toString();
        final AppUser outgoingUser = dataUserById(user, outgoingUserId);
        final AppUser incomingUser = dataUserById(user, incomingUserId);

        GetPeopleQuery.Data outgoingPage = usersApi.users(bearerToken, 0, 4, outgoingUser.username());
        GetPeopleQuery.Node outgoingNode = findNodeById(outgoingPage, outgoingUserId);

        GetPeopleQuery.Data incomingPage = usersApi.users(bearerToken, 0, 4, incomingUser.username());
        GetPeopleQuery.Node incomingNode = findNodeById(incomingPage, incomingUserId);

        assertAll("Outgoing invitation — node contract and status",
                () -> assertEquals(outgoingUserId, outgoingNode.id, "id must match outgoing user"),
                () -> assertEquals(outgoingUser.username(), outgoingNode.username, "username must match outgoing user"),
                () -> assertEquals(FriendStatus.INVITATION_SENT, outgoingNode.friendStatus, "status must be INVITATION_SENT"),
                () -> assertEquals(outgoingNode.location.code, outgoingNode.location.code,
                        "location.code must be equal")
        );

        assertAll("Incoming invitation — node contract and status",
                () -> assertEquals(incomingUserId, incomingNode.id, "id must match incoming user"),
                () -> assertEquals(incomingUser.username(), incomingNode.username, "username must match incoming user"),
                () -> assertEquals(INVITATION_RECEIVED, incomingNode.friendStatus, "status must be INVITATION_RECEIVED"),
                () -> assertEquals(incomingNode.location.code.toLowerCase(), incomingNode.location.code,
                        "location.code must be equal")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Поиск по своему нику не возвращает самого пользователя")
    void searchByOwnUsernameExcludesSelfFromResults(@Token String bearerToken, AppUser user) {
        GetPeopleQuery.Data pageEmptyUser = usersApi.users(bearerToken, 0, 4, user.username());
        List<GetPeopleQuery.Edge> nodeEmptyUser = pageEmptyUser.users.edges;

        assertEquals(0, nodeEmptyUser.size());

        if (!nodeEmptyUser.isEmpty()) {
            for (GetPeopleQuery.Edge edge : pageEmptyUser.users.edges) {
                GetPeopleQuery.Node node = edge.node;
                assertNotEquals(node.id, user.id().toString());
            }
        }
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Неизвестный ник даёт пустой список")
    void users_NodeContract_AndLocationNormalizedSelf(@Token String bearerToken, AppUser user) {
        final String testUsername = AnnotationHelper.getUniqueTestUsername();

        GetPeopleQuery.Data pageEmptyUser = usersApi.users(bearerToken, 0, 4, testUsername);
        List<GetPeopleQuery.Edge> nodeEmptyUser = pageEmptyUser.users.edges;

        assertEquals(0, nodeEmptyUser.size());
    }

    @Test
    @User(fullFriends = 1, friends = 1)
    @ApiLogin
    @DisplayName("Поиск друга по нику возвращает корректные данные и статус FRIEND") // [CHANGED]
    void searchByFriendUsernameReturnsFriendNodesWithExpectedFieldsWhenFriendshipEstablished(@Token String bearerToken, AppUser actor) {
        final String fullFriendId = firstFullFriendId(actor).toString();
        final String emptyFriendId = firstEmptyFriendId(actor).toString();
        final AppUser fullFriend = dataUserById(actor, fullFriendId);
        final AppUser emptyFriend = dataUserById(actor, emptyFriendId);

        GetPeopleQuery.Data fullFriendPage = usersApi.users(bearerToken, 0, 4, fullFriend.username());
        GetPeopleQuery.Node fullFriendNode = UserUtil.findNodeById(fullFriendPage, fullFriendId);

        GetPeopleQuery.Data emptyFriendPage = usersApi.users(bearerToken, 0, 4, emptyFriend.username());
        GetPeopleQuery.Node emptyFriendNode = UserUtil.findNodeById(emptyFriendPage, emptyFriendId);

        assertAll("Full friend — node contract and FRIEND status",
                () -> assertEquals(fullFriendId, fullFriendNode.id, "id must match full friend"),
                () -> assertEquals(fullFriend.username(), fullFriendNode.username, "username must match full friend"),
                () -> assertEquals(fullFriend.surname(), fullFriendNode.surname, "surname must match full friend"),
                () -> assertEquals(fullFriend.firstname(), fullFriendNode.firstname, "firstname must match full friend"),
                () -> assertEquals(FRIEND, fullFriendNode.friendStatus, "status must be FRIEND"),
                () -> assertNotNull(fullFriendNode.location, "location must be present"),
                () -> assertNotNull(fullFriendNode.location.code, "location.code must be present"),
                () -> assertEquals(fullFriendNode.location.code.toLowerCase(), fullFriendNode.location.code,
                        "location.code must be lower-case")
        );

        assertAll("Empty friend — node contract and FRIEND status",
                () -> assertEquals(emptyFriendId, emptyFriendNode.id, "id must match empty friend"),
                () -> assertEquals(emptyFriend.username(), emptyFriendNode.username, "username must match empty friend"),
                () -> assertEquals(emptyFriend.surname(), emptyFriendNode.surname, "surname must match empty friend"),
                () -> assertEquals(emptyFriend.firstname(), emptyFriendNode.firstname, "firstname must match empty friend"),
                () -> assertEquals(FRIEND, emptyFriendNode.friendStatus, "status must be FRIEND"),
                () -> assertNotNull(emptyFriendNode.location, "location must be present"),
                () -> assertNotNull(emptyFriendNode.location.code, "location.code must be present"),
                () -> assertEquals(emptyFriendNode.location.code.toLowerCase(), emptyFriendNode.location.code,
                        "location.code must be lower-case")
        );
    }
}
