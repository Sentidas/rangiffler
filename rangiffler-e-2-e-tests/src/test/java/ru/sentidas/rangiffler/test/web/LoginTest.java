package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;

import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.LoginPage;

@WebTest()
public class LoginTest {

    @Test
    @User
    void successLogin(AppUser user) {
        System.out.println("вывод по userId: " + user);
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username(), "12345")
                .successSubmit();
    }

    @Test
    @User
    void errorLoginPassword(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username(), "1234")
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }

    @Test
    @User
    void errorLoginUsername(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username() + "error", "1234")
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }
}
