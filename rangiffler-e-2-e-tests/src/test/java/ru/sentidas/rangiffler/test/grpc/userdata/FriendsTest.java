package ru.sentidas.rangiffler.test.grpc.userdata;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Userdata: allFriendsPage")
public class FriendsTest extends BaseTest {

    @Test
    @DisplayName("Заявка в друзья - создаёт исходящий и входящий инвайты - когда отправляем приглашение")
    @User
    void createFriendshipRequestCreatesOutcomeAndIncomeWhenSendingInvite(AppUser user) {
        // создаём отдельного адресата (аннотацией либо фабрикой — здесь используем UpdateUser как “минимального”)
        final String inviteeUsername = "invitee_user_" + UUID.randomUUID();
        userdataBlockingStub.updateUser(UpdateUserRequest.newBuilder()
                .setUsername(inviteeUsername)
                .build());
        // получаем id адресата
        UserResponse invitee = userdataBlockingStub.currentUser(
                ru.sentidas.rangiffler.grpc.UsernameRequest.newBuilder().setUsername(inviteeUsername).build()
        );

        userdataBlockingStub.createFriendshipRequest(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(invitee.getId())
                        .build()
        );

        UsersPageResponse outcome = userdataBlockingStub.outcomeInvitations(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse income = userdataBlockingStub.incomeInvitations(
                UserPageRequest.newBuilder().setUsername(inviteeUsername).setPage(0).setSize(10).build()
        );

        assertAll("Invite created",
                () -> assertTrue(ids(outcome).contains(invitee.getId()), "invitee must be in outcome invitations"),
                () -> assertTrue(ids(income).contains(user.id().toString()), "initiator must be in invitee's income invitations")
        );
    }

    @Test
    @DisplayName("Принятие инвайта - создаёт взаимную дружбу и очищает инвайты - когда адресат соглашается")
    @User(incomeInvitation = 1)
    void acceptFriendshipCreatesMutualFriendshipAndClearsInvitesWhenInviteeAccepts(AppUser user) {
        final String inviterId = user.testData().incomeInvitations().getFirst().id().toString();

        userdataBlockingStub.acceptFriendshipRequest(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(inviterId)
                        .build()
        );

        UsersPageResponse myFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse inviterFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(user.testData().incomeInvitations().getFirst().username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse myIncome = userdataBlockingStub.incomeInvitations(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse inviterOutcome = userdataBlockingStub.outcomeInvitations(
                UserPageRequest.newBuilder().setUsername(user.testData().incomeInvitations().getFirst().username()).setPage(0).setSize(10).build()
        );

        assertAll("After accept",
                () -> assertTrue(ids(myFriends).contains(inviterId), "inviter must be in my friends"),
                () -> assertTrue(ids(inviterFriends).contains(user.id().toString()), "I must be in inviter friends"),
                () -> assertEquals(0, myIncome.getContentCount(), "my income invites should be empty"),
                () -> assertFalse(ids(inviterOutcome).contains(user.id().toString()), "inviter outcome should not contain me")
        );
    }

    @Test
    @DisplayName("Отклонение инвайта - удаляет заявку без дружбы - когда адресат отклоняет")
    @User(incomeInvitation = 1)
    void declineFriendshipRemovesInviteWithoutFriendshipWhenInviteeDeclines(AppUser user) {
        final String inviterId = user.testData().incomeInvitations().getFirst().id().toString();

        userdataBlockingStub.declineFriendshipRequest(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(inviterId)
                        .build()
        );

        UsersPageResponse myIncome = userdataBlockingStub.incomeInvitations(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse myFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );

        assertAll("After decline",
                () -> assertEquals(0, myIncome.getContentCount(), "my income invites should be empty after decline"),
                () -> assertFalse(ids(myFriends).contains(inviterId), "there should be no friendship after decline")
        );
    }

    @Test
    @DisplayName("Удаление друга - удаляет связь у обоих - когда дружба уже установлена")
    @User(friends = 2)
    void removeFriendDeletesBondOnBothSidesWhenFriendshipEstablished(AppUser user) {
        final String removedFriendId = user.testData().friends().getFirst().id().toString();
        final String removedFriendUsername = user.testData().friends().getFirst().username();

        userdataBlockingStub.removeFriend(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(removedFriendId)
                        .build()
        );

        UsersPageResponse myFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        UsersPageResponse hisFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(removedFriendUsername).setPage(0).setSize(10).build()
        );

        assertAll("After remove",
                () -> assertFalse(ids(myFriends).contains(removedFriendId), "removed friend should disappear from my friends"),
                () -> assertFalse(ids(hisFriends).contains(user.id().toString()), "I should disappear from friend's friends")
        );
    }

    @Test
    @DisplayName("Удаление друга - безопасно повторяется - когда связи уже нет")
    @User(friends = 1)
    void removeFriendIsIdempotentWhenBondAlreadyRemoved(AppUser user) {
        final String friendId = user.testData().friends().getFirst().id().toString();

        userdataBlockingStub.removeFriend(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(friendId)
                        .build()
        );
        // повторно
        userdataBlockingStub.removeFriend(
                FriendshipRequest.newBuilder()
                        .setUsername(user.username())
                        .setUser(friendId)
                        .build()
        );

        UsersPageResponse myFriends = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder().setUsername(user.username()).setPage(0).setSize(10).build()
        );
        assertAll("After double remove",
                () -> assertFalse(ids(myFriends).contains(friendId), "friend should still be absent")
        );
    }

    @Test
    @DisplayName("Удаление друга_targetId не существует: NOT_FOUND")
    @User
    void removeFriendReturnsNotFoundWhenTargetDoesNotExist(AppUser user) {
        FriendshipRequest request = FriendshipRequest.newBuilder()
                .setUsername(user.username())
                .setUser(java.util.UUID.randomUUID().toString())
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> userdataBlockingStub.removeFriend(request));

        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode(), "status should be NOT_FOUND");
    }

    private static Set<String> ids(UsersPageResponse page) {
        return page.getContentList().stream()
                .map(UserResponse::getId)
                .collect(Collectors.toSet());
    }
}
