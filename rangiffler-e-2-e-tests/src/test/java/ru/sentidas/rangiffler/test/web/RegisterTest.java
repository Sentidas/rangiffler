package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.page.LoginPage;
import ru.sentidas.rangiffler.page.RegisterPage;
import ru.sentidas.rangiffler.utils.generator.RandomDataUtils;

public class RegisterTest {

    @Test
    void successRegistration() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        RandomDataUtils.randomName(),
                        "12345",
                        "12345"
                ).successSubmit();
    }

    @Test
    void successRegistration2() {
        final String username = RandomDataUtils.randomUsername();
        final String password = "12345";
        final String submitPassword = "12345";
        System.out.println(username);

        Selenide.open(LoginPage.URL, LoginPage.class)
                .clickButtonRegister()
                .fillRegisterPage(
                        username,
                        password,
                        submitPassword
                ).successSubmit()
                .goLoginPage()
                .fillLoginPage(
                        username,
                        password
                )
                .successSubmit();
    }
}
