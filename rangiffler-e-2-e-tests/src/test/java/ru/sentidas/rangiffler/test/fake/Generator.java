package ru.sentidas.rangiffler.test.fake;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.utils.generator.UserData;
import ru.sentidas.rangiffler.utils.generator.UserDataGenerator;

@WebTest
public class Generator {

    @Test
    void generator() {
        for (int i = 0; i < 50; i++) {
            UserData userData = UserDataGenerator.randomUser();
            System.out.println(userData.countryCode());
            System.out.println(userData.firstname());
            System.out.println(userData.surname());
        }
    }

    @Test
    @User(
            photos = {
                    @Photo(),
                    @Photo(), @Photo()
            }, friends = 3, incomeInvitation = 4, outcomeInvitation = 2)
    void user(AppUser user) {
        System.out.println(user.username());
        System.out.println(user.firstname());
        System.out.println(user.surname());
        System.out.println(user.countryCode());
    }

    @Test
    @User
    @ApiLogin
    void user1(AppUser user) {
        System.out.println(user.username());
        System.out.println(user.firstname());
        System.out.println(user.surname());
        System.out.println(user.countryCode());
    }

    @Test
    @User(
            photos = {
                    @Photo(likes = 1),
                    @Photo(likes = 1),
                    @Photo(likes = 1),
                    @Photo(likes = 1),
                    @Photo()

            }, friends = 3, incomeInvitation = 4, outcomeInvitation = 2)
    void user9(AppUser user) {
        System.out.println(user.username());
        System.out.println(user.firstname());
        System.out.println(user.surname());
        System.out.println(user.countryCode());
    }

    @Test
    @User(
            photo = 16, friends = 3, incomeInvitation = 4, outcomeInvitation = 2)
    void user5(AppUser user) {
        System.out.println(user.username());
        System.out.println(user.firstname());
        System.out.println(user.surname());
        System.out.println(user.countryCode());
    }

}
