package ru.sentidas.rangiffler.test.web.people;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.PeoplePage;
import ru.sentidas.rangiffler.service.UsersApiClient;
import ru.sentidas.rangiffler.service.UsersDbClient;

import static ru.sentidas.rangiffler.page.PeopleTab.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@WebTest
public class PeopleTest {

    private final UsersDbClient usersDbClient = new UsersDbClient();

    @Test
    @User
    @ApiLogin
    @DisplayName("Текущий пользователь не отображается на вкладках: 'Все', 'Друзья', 'Входящие', 'Исходящие'")
    void currentUserIsAbsentInAllTabs(AppUser user) {
        final String username = user.username();

        Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(ALL)
                .checkNoExistingUser(username)
                .openTab(FRIENDS)
                .checkNoExistingUser(username)
                .openTab(INCOME)
                .checkNoExistingUser(username)
                .openTab(OUTCOME)
                .checkNoExistingUser(username);
    }

    @Test
    @User(friends = 2, incomeInvitation = 2, outcomeInvitation = 2)
    @ApiLogin
    @DisplayName("Соответствующие пользователи отображаются на вкладках: 'Все', 'Друзья', 'Входящие', 'Исходящие'")
    void friendsIncomeOutcomeContainExpectedUsers(AppUser user) {
        final String friendUsername = firstFriendUsername(user);
        final String incomeUsername = incomingInviterUsername(user);
        final String outcomeUsername = outgoingInviteeUsername(user);

        Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(FRIENDS)
                .checkExistingUser(friendUsername, FRIENDS)
                .openTab(INCOME)
                .checkExistingUser(incomeUsername, INCOME)
                .openTab(OUTCOME)
                .checkExistingUser(outcomeUsername, OUTCOME);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Отправка приглашения новому пользователю: появляется в 'Исходящих', отображается алерт")
    void sendInvitationCreatesOutcomeAndShowsAlert() {
        final String invitedUsername = getUniqueTestUsername();
        usersDbClient.createUser(invitedUsername, "12345");

        PeoplePage people = Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(ALL)
                .sendFriendInvitationToUser(invitedUsername)
                .checkAlert("Invitation sent")
                .checkInvitationSentToUser(invitedUsername);

        people.openTab(OUTCOME)
                .checkInvitationSentToUser(invitedUsername);
    }

    @Test
    @User(incomeInvitation = 2)
    @ApiLogin
    @DisplayName("Принятие приглашения из 'Входящих': пользователь появляется в 'Друзьях' и после рефреша исчезает из списка 'Входящие'")
    void acceptInvitationMovesUserToFriends(AppUser user) {
        final String invitingUsername = incomingInviterUsername(user);

        PeoplePage people = Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(INCOME)
                .acceptInvitationToUser(invitingUsername)
                .checkAlert("Invitation accepted");

        people.openTab(FRIENDS)
                .checkExistingUser(invitingUsername, FRIENDS)
                .checkFriend(invitingUsername);

        Selenide.refresh();

        people.openTab(INCOME)
                .checkNoExistingUser(invitingUsername);
    }

    @Test
    @User(incomeInvitation = 2)
    @ApiLogin
    @DisplayName("Отклонение приглашения: пользователь исчезает из 'Входящих' и не появляется в 'Друзьях'")
    void declineInvitationRemovesUserFromIncome(AppUser user) {
        final String invitingUsername = incomingInviterUsername(user);

        final PeoplePage people = Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(INCOME)
                .declineInvitationToUser(invitingUsername)
                .checkAlert("Invitation declined")
                .checkNoExistingUser(invitingUsername);

        people.openTab(FRIENDS)
                .checkNoExistingUser(invitingUsername);
    }

    @Test
    @User(friends = 1)
    @ApiLogin
    @DisplayName("Удаление друга: исчезает из 'Друзей', показывается алерт")
    void removeFriendDeletesFromFriendsTab(AppUser user) {
        final String friendToRemove = firstFriendUsername(user);

        Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(FRIENDS)
                .checkExistingUser(friendToRemove, FRIENDS)
                .removeFriend(friendToRemove)
                .checkAlert("Friend deleted")
                .checkNoExistingUser(friendToRemove);
    }
}
