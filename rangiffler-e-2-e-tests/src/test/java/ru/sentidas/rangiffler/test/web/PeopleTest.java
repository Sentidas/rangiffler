package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.PeoplePage;

import static ru.sentidas.rangiffler.page.PeopleTab.*;

@WebTest
public class PeopleTest {

    @Test
    @User
    @ApiLogin
    void checkUserNotExistInAllPeople(AppUser user) throws InterruptedException {
        System.out.println(user.username());
        String username = user.username();
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
    @User
    @ApiLogin
    void checkUserExistInAllPeople(AppUser user) throws InterruptedException {

        Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(FRIENDS)
                .checkExistingUser("mule16");
    }

    @Test
    @User
    @ApiLogin
    void sendInvitation(AppUser user) {
        System.out.println(user.username());

        PeoplePage people = Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(ALL);
        String friendUsername = people.sendFriendInvitationToFirstUser();
        System.out.println(friendUsername);
        people.checkInvitationSentToUser(friendUsername);

    }

    @Test
    @User
    @ApiLogin
    void sendInvitationAndCheckWaiting(AppUser user)  {
        System.out.println(user.username());

        PeoplePage people = Selenide.open(PeoplePage.URL, PeoplePage.class)
                .openTab(ALL);
        String friendUsername = people.sendFriendInvitationToFirstUser();
        System.out.println(friendUsername);
        people.checkInvitationSentToUser(friendUsername);

        Selenide.refresh();
        people.openTab(OUTCOME)
                .checkInvitationSentToUser(friendUsername);
    }
}
