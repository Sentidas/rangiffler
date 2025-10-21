package ru.sentidas.rangiffler.test.gql.mutation.user;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.FriendshipActionMutation;
import ru.sentidas.GetPeopleQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FriendshipApi;
import ru.sentidas.rangiffler.test.gql.api.UsersApi;
import ru.sentidas.rangiffler.test.gql.support.ErrorGql;
import ru.sentidas.rangiffler.test.gql.support.UserUtil;
import ru.sentidas.rangiffler.utils.AnnotationHelper;
import ru.sentidas.type.FriendStatus;
import ru.sentidas.type.FriendshipAction;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.ErrorGql.*;
import static ru.sentidas.rangiffler.test.gql.support.UserUtil.findNodeById;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;
import static ru.sentidas.type.FriendshipAction.*;

public class FriendshipActionGqlTest extends BaseGraphQlTest {

    private final FriendshipApi friendshipApi = new FriendshipApi(apolloClient);
    private final UsersApi usersApi = new UsersApi(apolloClient);
    private final UsersDbClient usersDbClient = new UsersDbClient();

    @Test
    @User
    @ApiLogin
    @DisplayName("Приглашение в друзья: у инициатора статус — INVITATION_SENT")
    void friendshipAddSetsInvitationSentForRequesterWhenInvitingCandidate(@Token String bearerToken) {
        final String candidateUsername = getUniqueTestUsername();
        final AppUser candidateUser = usersDbClient.createUser(candidateUsername, "12345");
        final String candidateId = candidateUser.id().toString();

        // Действие: отправка приглашения в друзья
        FriendshipActionMutation.Data invitationResponse = friendshipApi.sentInvitation(bearerToken, candidateId);

        assertAll("ADD response fields — invitation created",
                () -> assertEquals(candidateId, invitationResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(candidateUsername, invitationResponse.friendship.username, "username mismatch"),
                () -> assertEquals(FriendStatus.INVITATION_SENT, invitationResponse.friendship.friendStatus, "status mismatch")
        );

        // Пост-проверка INVITATION_SENT в users()
        GetPeopleQuery.Data afterPage = usersApi.users(bearerToken, 0, 10, candidateUsername);
        GetPeopleQuery.Node candidateNode = findNodeById(afterPage, candidateId);
        assertEquals(FriendStatus.INVITATION_SENT, candidateNode.friendStatus);
    }

    @Test
    @User(friends = 1)
    @ApiLogin
    @DisplayName("Удаление из друзей: больше не FRIEND, статус очищен")
    void friendshipDeleteRemovesFromFriendsAndClearsStatusWhenFriendExists(@Token String bearerToken, AppUser user) {
        final String friendId = friendIdtoString(user);
        final String friendUsername = friendUsername(user);

        // Действие: удаление друга
        FriendshipActionMutation.Data deleteResponse =
                friendshipApi.deleteFriend(bearerToken, friendId.toString());

        assertAll("DELETE response fields — friend removed, status cleared",
                () -> assertEquals(friendId, deleteResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(friendUsername, deleteResponse.friendship.username, "username mismatch"),
                () -> assertNull(deleteResponse.friendship.friendStatus, "status must be null after DELETE")
        );

        // Пост-проверка null в users()
        GetPeopleQuery.Data afterPage = usersApi.users(bearerToken, 0, 10, friendUsername);
        GetPeopleQuery.Node node = findNodeById(afterPage, friendId);
        assertEquals(null, node.friendStatus, "friendStatus must be null after DELETE");

    }

    @Test
    @User(incomeInvitation = 1)
    @ApiLogin
    @DisplayName("Принятие приглашения: статус становится FRIEND")
    void friendshipAcceptMakesUsersFriendsWhenIncomingInvitationExists(@Token String bearerToken, AppUser user) {
        final String inviterId = incomingInviterIdToString(user);
        final String inviterUsername = incomingInviterUsername(user);

        // Подготовка: INVITATION_SENT в users() до принятия приглашения
        GetPeopleQuery.Data beforePage  = usersApi.users(bearerToken, 0, 10, inviterUsername);
        final GetPeopleQuery.Node beforeNode = findNodeById(beforePage, inviterId);
        assertEquals(FriendStatus.INVITATION_RECEIVED, beforeNode.friendStatus);

        // Действие: принятие приглашения
        FriendshipActionMutation.Data acceptResponse  = friendshipApi.accept(bearerToken, inviterId);
        assertAll("ACCEPT response fields — users become friends",
                () -> assertEquals(inviterId, acceptResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(inviterUsername, acceptResponse.friendship.username, "username mismatch"),
                () -> assertEquals(FriendStatus.FRIEND, acceptResponse.friendship.friendStatus, "status must be FRIEND after ACCEPT")
        );

        // Пост-проверка FRIEND в users()
        GetPeopleQuery.Data afterPage  = usersApi.users(bearerToken, 0, 10, inviterUsername);
        GetPeopleQuery.Node afterNode = findNodeById(afterPage, inviterId);
        assertEquals(FriendStatus.FRIEND, afterNode.friendStatus);
    }

    @Test
    @User(incomeInvitation = 1)
    @ApiLogin
    @DisplayName("Отклонение приглашения: приглашение исчезает, дружбы нет")
    void friendshipRejectDropsInvitationAndClearsStatusWhenIncomingInvitationExists(@Token String bearerToken, AppUser user) {
        final String inviterId = AnnotationHelper.incomingInviterId(user).toString();
        final String inviterUsername = incomingInviterUsername(user);

        // Подготовка: INVITATION_SENT в users() до отклонения приглашения
        GetPeopleQuery.Data beforePage  = usersApi.users(bearerToken, 0, 10, inviterUsername);
        final GetPeopleQuery.Node beforeNode = findNodeById(beforePage, inviterId);
        assertEquals(FriendStatus.INVITATION_RECEIVED, beforeNode.friendStatus);

        // Действие: отклонение приглашения
        FriendshipActionMutation.Data rejectResponse  = friendshipApi.reject(bearerToken, inviterId);
        assertAll("REJECT response fields — invitation removed",
                () -> assertEquals(inviterId, rejectResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(inviterUsername, rejectResponse.friendship.username, "username mismatch"),
                () -> assertNull(rejectResponse.friendship.friendStatus, "status must be null after REJECT")
        );

        // Пост-проверка null в users()
        GetPeopleQuery.Data afterPage  = usersApi.users(bearerToken, 0, 10, inviterUsername);
        final GetPeopleQuery.Node afterNode = UserUtil.findNodeById(afterPage, inviterId);
        assertEquals(null, afterNode.friendStatus, "friendStatus must be null after DELETE");
    }

    @Test
    @User(outcomeInvitation = 1)
    @ApiLogin
    @DisplayName("Повторное приглашение того же пользователя: остаётся INVITATION_SENT")
    void friendshipAddDuplicateInvitationKeepsInvitationSentWhenAlreadyInvited(@Token String bearerToken, AppUser user) {
        final String inviteeId = outgoingInviteeId(user).toString();
        final String inviteeUsername = outgoingInviteeUsername(user);

        ApolloResponse<FriendshipActionMutation.Data> response  =
                friendshipApi.tryAction(bearerToken, inviteeId, FriendshipAction.ADD);
        assertEquals(FriendStatus.INVITATION_SENT, response.data.friendship.friendStatus);


        GetPeopleQuery.Data usersPage = usersApi.users(bearerToken, 0, 10, inviteeUsername);
        GetPeopleQuery.Node inviteeNode = usersPage .users.edges.getFirst().node;
        assertEquals(FriendStatus.INVITATION_SENT, inviteeNode.friendStatus);
    }

    @Test
    @User(friends = 1)
    @ApiLogin
    @DisplayName("Отправка приглашения уже другу: остается FRIEND")
    void friendshipAddForExistingFriendReturnsFriend(@Token String bearerToken, AppUser user) {
        final String friendId = AnnotationHelper.firstFriendId(user).toString();
        final String friendUsername = AnnotationHelper.firstFriendUsername(user);

        // Повторный ADD к уже другу — подтверждение FRIEND
        FriendshipActionMutation.Data invitationResponse  = friendshipApi.sentInvitation(bearerToken, friendId);
        assertAll("ADD response fields — already friends remain FRIEND",
                () -> assertEquals(friendId, invitationResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(friendUsername, invitationResponse.friendship.username, "username mismatch"),
                () -> assertEquals(FriendStatus.FRIEND, invitationResponse.friendship.friendStatus,
                        "status must be FRIEND for existing friend")
        );

        // в users() остается FRIEND
        GetPeopleQuery.Data page = usersApi.users(bearerToken, 0, 10, friendUsername);
        assertEquals(FriendStatus.FRIEND, findNodeById(page, friendId).friendStatus);
    }

    @Test
    @User(outcomeInvitation = 1)
    @ApiLogin
    @DisplayName("REJECT своей исходящей заявки: остаётся INVITATION_SENT")
    void friendshipRejectForOwnOutgoingInvitationIsNoOpAndKeepsInvitationSent(@Token String bearerToken, AppUser user) {
        final String inviteeId = outgoingInviteeId(user).toString();
        final String inviteeUsername = outgoingInviteeUsername(user);

        // Предусловие: статус в user INVITATION_SENT
        GetPeopleQuery.Data beforePage  = usersApi.users(bearerToken, 0, 10, inviteeUsername);
        assertEquals(FriendStatus.INVITATION_SENT, findNodeById(beforePage , inviteeId).friendStatus);

        // REJECT своей исходящей — не должен ничего удалять
        FriendshipActionMutation.Data rejectResponse  = friendshipApi.reject(bearerToken, inviteeId);
        assertAll("REJECT on own outgoing — no-op, status preserved",
                () -> assertEquals(inviteeId, rejectResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(inviteeUsername, rejectResponse.friendship.username, "username mismatch"),
                () -> assertEquals(FriendStatus.INVITATION_SENT, rejectResponse.friendship.friendStatus,
                        "rejecting own outgoing invitation must keep INVITATION_SENT")
        );

        // Постусловие: статус в users() неизменен INVITATION_SENT
        GetPeopleQuery.Data afterPage  = usersApi.users(bearerToken, 0, 10, inviteeUsername);
        assertEquals(FriendStatus.INVITATION_SENT, findNodeById(afterPage, inviteeId).friendStatus);
    }

    @Test
    @User(incomeInvitation = 1)
    @ApiLogin
    @DisplayName("REJECT входящего приглашения, затем повторный ADD вновь создаёт 'INVITATION_SENT'")
    void friendshipAddRejectThenAddAgainCreatesInvitationSent(@Token String bearerToken, AppUser user) {
        final String inviterId = incomingInviterIdToString(user);
        final String inviterUsername = incomingInviterUsername(user);

        // Исходный статус INVITATION_RECEIVED
        GetPeopleQuery.Data before = usersApi.users(bearerToken, 0, 10, inviterUsername);
        assertEquals(FriendStatus.INVITATION_RECEIVED, findNodeById(before, inviterId).friendStatus);

        // REJECT входящего приглашения
        FriendshipActionMutation.Data rejectResponse  = friendshipApi.reject(bearerToken, inviterId);
        assertAll("REJECT response fields — invitation removed",
                () -> assertEquals(inviterId, rejectResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(inviterUsername, rejectResponse.friendship.username, "username mismatch"),
                () -> assertNull(rejectResponse.friendship.friendStatus, "status must be null after REJECT")
        );

        GetPeopleQuery.Data pageAfterReject = usersApi.users(bearerToken, 0, 10, inviterUsername);
        assertEquals(null, findNodeById(pageAfterReject, inviterId).friendStatus);

        // Снова ADD к тому же пользователю — должен появиться исходящий и вернуться INVITATION_SENT
        FriendshipActionMutation.Data addResponse  = friendshipApi.sentInvitation(bearerToken, inviterId);
        assertAll("ADD-after-REJECT response fields — invitation re-created",
                () -> assertEquals(inviterId, addResponse.friendship.id, "friend id mismatch"),
                () -> assertEquals(inviterUsername, addResponse.friendship.username, "username mismatch"),
                () -> assertEquals(FriendStatus.INVITATION_SENT, addResponse.friendship.friendStatus,
                        "status must be INVITATION_SENT after ADD again")
        );

        // И в users() — INVITATION_SENT
        GetPeopleQuery.Data pageAfterAdd  = usersApi.users(bearerToken, 0, 10, inviterUsername);
        assertEquals(FriendStatus.INVITATION_SENT, findNodeById(pageAfterAdd, inviterId).friendStatus);
    }

    // ==== Негативные сценарии ====

    @Test
    @User
    @ApiLogin
    @DisplayName("Приглашение самому себе: FORBIDDEN, статус не меняется")
    void friendshipAddSelfFailsWithForbiddenAndDoesNotChangeStatus(@Token String bearerToken, AppUser user) {
        ApolloResponse<FriendshipActionMutation.Data> response =
                friendshipApi.tryAction(bearerToken, user.id().toString(), FriendshipAction.ADD);

        assertAll("FORBIDDEN on self-invitation",
                () -> assertTrue(response.hasErrors(), "expected error presence"),
                () -> assertEquals("FORBIDDEN", classification(response), "expected FORBIDDEN"),
                () -> assertEquals("friendship", path(response), "error path mismatch"),
                () -> assertEquals("Cannot create friendship request for self user", message(response), "error message mismatch")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("ACCEPT несуществующего пользователя: NOT_FOUND")
    void friendshipAcceptNonExistingUserReturnsNotFound(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();
        ApolloResponse<FriendshipActionMutation.Data> response =
                friendshipApi.tryAction(bearerToken, randomId, ACCEPT);

        assertAll("NOT_FOUND on ACCEPT for non-existing user",
                () -> assertTrue(response.hasErrors(), "expected error presence"),
                () -> assertEquals("NOT_FOUND", classification(response), "expected NOT_FOUND"),
                () -> assertEquals("friendship", path(response), "error path mismatch"),
                () -> assertEquals("Cannot find user by ID: " + randomId, message(response), "error message mismatch")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("REJECT несуществующего пользователя: NOT_FOUND")
    void friendshipRejectNonExistingUserReturnsNotFound(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();
        ApolloResponse<FriendshipActionMutation.Data> response =
                friendshipApi.tryAction(bearerToken, randomId, REJECT);

        assertAll("NOT_FOUND on REJECT for non-existing user",
                () -> assertTrue(response.hasErrors(), "expected error presence"),
                () -> assertEquals("NOT_FOUND", ErrorGql.classification(response), "expected NOT_FOUND"),
                () -> assertEquals("friendship", ErrorGql.path(response), "error path mismatch"),
                () -> assertEquals("Cannot find user by ID: " + randomId, ErrorGql.message(response), "error message mismatch")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("DELETE несуществующего пользователя: NOT_FOUND")
    void friendshipDeleteNonExistingUserReturnsNotFound(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();
        ApolloResponse<FriendshipActionMutation.Data> response =
                friendshipApi.tryAction(bearerToken, randomId, DELETE);

        assertAll("NOT_FOUND on DELETE for non-existing user",
                () -> assertTrue(response.hasErrors(), "expected error presence"),
                () -> assertEquals("NOT_FOUND", ErrorGql.classification(response), "expected NOT_FOUND"),
                () -> assertEquals("friendship", ErrorGql.path(response), "error path mismatch"),
                () -> assertEquals("Cannot find user by ID: " + randomId, ErrorGql.message(response), "error message mismatch")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Приглашение несуществующего пользователя: NOT_FOUND")
    void friendshipAddNonExistingUserReturnsNotFound(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();

        ApolloResponse<FriendshipActionMutation.Data> response =
                friendshipApi.tryAction(bearerToken, randomId, FriendshipAction.ADD);

        assertAll("NOT_FOUND on ADD for non-existing user",
                () -> assertTrue(response.hasErrors(), "expected error presence"),
                () -> assertEquals("NOT_FOUND", ErrorGql.classification(response), "expected NOT_FOUND"),
                () -> assertEquals("friendship", ErrorGql.path(response), "error path mismatch"),
                () -> assertEquals("Cannot find user by ID: " + randomId, ErrorGql.message(response), "error message mismatch")
        );
    }
}
