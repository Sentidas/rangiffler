package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.page.LoginPage;
import ru.sentidas.rangiffler.page.RegisterPage;
import ru.sentidas.rangiffler.utils.RandomDataUtils;

public class LoginTests {

    @Test
    void successRegistration() {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage("duck", "12345")
                .successSubmit();
    }

    @Test
    void errorRegistration() {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage("duck", "1234")
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }
}
