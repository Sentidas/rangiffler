package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.page.LoginPage;

@WebTest()
public class LoginTests {

    @Test
    @User(friends = 1)
    void successRegistration(User user) {
        System.out.println("вывод по user: " + user);
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage("duck", "12345")
                .successSubmit();
    }

    @Test
    @User(friends = 1)
    void errorRegistration() {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage("duck", "1234")
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }
}
