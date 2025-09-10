package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class RegisterPage extends BasePage<RegisterPage> {

    public static final String URL = GFG.authUrl() + "register";

    private final SelenideElement usernameInput = $("#username"),
            passwordInput = $("#password"),
            passwordSubmitInput = $("#passwordSubmit"),
            submitButton = $(".form__submit"),
            proceedLoginButton = $(".form_sign-in"),
            errorAlert = $(".form__error");


    @Step("Fill register page with credentials: username: {0}, password: {1}, submit password: {2}")
    @Nonnull
    public RegisterPage fillRegisterPage(String login, String password, String passwordSubmit) {
        setUsername(login);
        setPassword(password);
        setSubmitPassword(passwordSubmit);
        return this;
    }

    @Step("Set username: '{0}'")
    @Nonnull
    public RegisterPage setUsername(String login) {
       usernameInput.setValue(login);
       return this;
    }

    @Step("Set password: '{0}'")
    @Nonnull
    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Confirm password: '{0}'")
    @Nonnull
    public RegisterPage setSubmitPassword(String passwordSubmit) {
        passwordSubmitInput.setValue(passwordSubmit);
        return this;
    }

    @Step("Submit success registration")
    @Nonnull
    public LoginPage successSubmit() {
        submitButton.click();
        proceedLoginButton.click();
        return new LoginPage();
    }

    @Step("Submit error registration")
    @Nonnull
    public RegisterPage errorSubmit(String passwordSubmit) {
        submitButton.click();
        return this;
    }

    @Step("Check alert message")
    @Nonnull
    public RegisterPage checkAlertMessage(String alertMessage) {
        errorAlert.shouldHave(text(alertMessage));
        return this;
    }
}
