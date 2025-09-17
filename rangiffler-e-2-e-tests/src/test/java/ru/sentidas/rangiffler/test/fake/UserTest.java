package ru.sentidas.rangiffler.test.fake;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.sentidas.rangiffler.jupiter.extension.UsersClientExtension;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.service.UsersClient;

@ExtendWith(UsersClientExtension.class)
public class UserTest {

    private UsersClient usersClient;


    @Test
    void createUser() {
        usersClient.createUser(
                "duck67", "12345"
        );
    }

    @Test
    void removeUser() {
        usersClient.removeUser("dd7");
    }

    @Test
    void updateUser() {
        AppUser updatedUser =
                new AppUser(
                        null,
                        "duck",
                        "murk",
                        "angry catty",
                        null,
                        null,
                        null,
                        "CN",
                        null
                );
        usersClient.updateUser("duck", updatedUser);
    }


    @Test
    void createOutcomeInvitations() {
        AppUser targetUser =
                new AppUser(
                        null,
                        "duck",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
        usersClient.createOutcomeInvitations(targetUser, 2);
    }

    @Test
    void createIncomeInvitations() {
        AppUser targetUser =
                new AppUser(
                        null,
                        "fox",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
        usersClient.createIncomeInvitations(targetUser, 1);
    }

    @Test
    void addFriends() {
        AppUser targetUser =
                new AppUser(
                        null,
                        "mink41",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
        usersClient.addFriends(targetUser, 2);
    }
}



