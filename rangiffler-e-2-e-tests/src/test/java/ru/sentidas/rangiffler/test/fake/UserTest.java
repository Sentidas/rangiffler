package ru.sentidas.rangiffler.test.fake;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.sentidas.rangiffler.jupiter.extension.UsersClientExtension;
import ru.sentidas.rangiffler.model.User;
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
        User updatedUser =
                new User(
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
        User targetUser =
                new User(
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
        User targetUser =
                new User(
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
        User targetUser =
                new User(
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



