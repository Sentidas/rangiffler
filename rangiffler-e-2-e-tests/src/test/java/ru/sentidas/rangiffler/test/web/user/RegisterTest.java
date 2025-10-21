package ru.sentidas.rangiffler.test.web.user;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.LoginPage;
import ru.sentidas.rangiffler.page.RegisterPage;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;

import static ru.sentidas.rangiffler.utils.generation.GenerationDataUser.getUniqueTestUsername;
import static ru.sentidas.rangiffler.utils.generation.GenerationDataUser.randomSentenceByLength;

@WebTest
public class RegisterTest {

    private static final String PASSWORD_VALID = "12345";

    @Test
    @DisplayName("Регистрация по прямой ссылке: корректные данные приводят к успешной регистрации")
    void registerShouldSucceedWhenUserEntersValidData() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).successSubmit();
    }

    @Test
    @DisplayName("Регистрация со страницы логина: после успешной регистрации вход проходит с новыми данными")
    void loginShouldSucceedAfterFreshRegistrationWhenCredentialsAreValid() {
        final String username = GenerationDataUser.randomUsername();

        Selenide.open(LoginPage.URL, LoginPage.class)
                .clickButtonRegister()
                .fillRegisterPage(
                        username,
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).successSubmit()
                .goLoginPage()
                .fillLoginPage(
                        username,
                        PASSWORD_VALID
                )
                .successSubmit();
    }

    @Test
    @User
    @DisplayName("Регистрация по прямой ссылке: корректные данные приводят к успешной регистрации")
    void registerShouldSucceedWhenUserEntersValidDatad9() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        randomSentenceByLength(3),
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).successSubmit();
    }

    @Test
    @User
    @DisplayName("Регистрация: логин максимально допустимой длины (50) — регистрация успешна")
    void registerShouldSucceedWhenUsernameHasMaxAllowedLength() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        randomSentenceByLength(50),
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).successSubmit();
    }

    @Test
    @DisplayName("Регистрация: логин минимально допустимой длины (3) — регистрация успешна")
    void registerShouldSucceedWhenUsernameHasMinAllowedLength() {
        String minAllowedPassword  = randomSentenceByLength(3);
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        minAllowedPassword ,
                        minAllowedPassword
                ).successSubmit();
    }

    @Test
    @DisplayName("Регистрация по прямой ссылке: корректные данные приводят к успешной регистрации")
    void registerShouldSucceedWhenUserEntersValidData3ddee() {
        String maxAllowedPassword  = randomSentenceByLength(12);
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        maxAllowedPassword ,
                        maxAllowedPassword
                ).successSubmit();
    }

    // ==== Негативные сценарии: пароли, длина логина/пароля, уже занятый логин ====

    @Test
    @DisplayName("Регистрация: показывается ошибка при несовпадающих паролях")
    void registerShouldShowMismatchErrorWhenPasswordsDiffer() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        PASSWORD_VALID,
                        "1234"
                ).errorSubmit("Passwords should be equal");
    }

    @Test
    @User
    @DisplayName("Регистрация: показывается ошибка при уже занятом логине")
    void registerShouldShowAlreadyExistsErrorWhenUsernameIsTaken(AppUser user) {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        user.username(),
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).errorSubmit(" Username `" + user.username() + "` already exists user");
    }

    @Test
    @User
    @DisplayName("Регистрация: логин короче минимума — показывается ошибка о длине")
    void registerShouldShowUsernameLengthErrorWhenUsernameTooShort() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        "Zk",
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).errorSubmit("Allowed username length should be from 3 to 50 characters");
    }

    @Test
    @User
    @DisplayName("Регистрация: логин длиннее максимума — показывается ошибка о длине")
    void registerShouldShowUsernameLengthErrorWhenUsernameTooLong() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        randomSentenceByLength(51),
                        PASSWORD_VALID,
                        PASSWORD_VALID
                ).errorSubmit("Allowed username length should be from 3 to 50 characters");
    }


    @Test
    @DisplayName("Регистрация: пароль короче минимума — показывается ошибка о длине")
    void registerShouldShowPasswordLengthErrorWhenPasswordTooShort() {
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        "12",
                        "12"
                ).errorSubmit("Allowed password length should be from 3 to 12 characters");
    }

    @Test
    @DisplayName("Регистрация: пароль длиннее максимума — показывается ошибка о длине")
    void registerShouldShowPasswordLengthErrorWhenPasswordTooLong() {
        String overLimitPassword  = randomSentenceByLength(13);
        Selenide.open(RegisterPage.URL, RegisterPage.class)
                .fillRegisterPage(
                        getUniqueTestUsername(),
                        overLimitPassword ,
                        overLimitPassword
                ).errorSubmit("Allowed password length should be from 3 to 12 characters");
    }
}
