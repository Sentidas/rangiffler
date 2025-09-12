package ru.sentidas.rangiffler.test.fake;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.utils.generator.UserData;
import ru.sentidas.rangiffler.utils.generator.UserDataGenerator;

@WebTest
public class Generator {

    @Test
    void generator(){
        for (int i = 0; i < 50; i++) {
            UserData userData = UserDataGenerator.randomUser();
            System.out.println(userData.countryCode());
            System.out.println(userData.firstName());
            System.out.println(userData.surname());
        }
    }

    @Test
    @User(friends = 4, incomeInvitation = 2, outcomeInvitation = 3)
    void user(ru.sentidas.rangiffler.model.User user){
        System.out.println(user.username());
        System.out.println(user.firstname());
        System.out.println(user.surname());
        System.out.println(user.countryCode());
    }
}
