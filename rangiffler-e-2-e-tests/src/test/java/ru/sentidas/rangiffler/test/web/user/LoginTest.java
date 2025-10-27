package ru.sentidas.rangiffler.test.web.user;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.LoginPage;

@WebTest
@DisplayName("Web_Авторизация")
public class LoginTest {

    private static final String PASSWORD_VALID = "12345";
    private static final String PASSWORD_INVALID = "1234";

    @Test
    @User
    @DisplayName("Вход с валидными данными завершается успешно")
    void loginShouldSucceedWhenValidCredentialsProvided(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username(), PASSWORD_VALID)
                .successSubmit();
    }

    @Test
    @User
    @DisplayName("Вход с неверным паролем показывает ошибку 'Неверные учетные данные пользователя'")
    void loginShouldFailWithErrorWhenPasswordIsInvalid(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username(), PASSWORD_INVALID)
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }

    @Test
    @User
    @DisplayName("Вход с неверным логином показывает ошибку 'Неверные учетные данные пользователя")
    void loginShouldFailWithErrorWhenUsernameIsInvalid(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username() + "error", PASSWORD_VALID)
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }

    @Test
    @User
    @DisplayName("Вход с неверным логином и паролем показывает ошибку 'Неверные учетные данные пользователя")
    void loginShouldFailWithErrorWhenUsernameAndPasswordAreInvalid(AppUser user) {
        Selenide.open(LoginPage.URL, LoginPage.class)
                .goLoginPage()
                .fillLoginPage(user.username() + "error", PASSWORD_INVALID)
                .errorSubmit()
                .checkAlertMessage("Неверные учетные данные пользователя");
    }
}
