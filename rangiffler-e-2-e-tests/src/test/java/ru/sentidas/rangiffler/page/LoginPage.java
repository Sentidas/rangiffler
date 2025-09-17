package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import lombok.extern.java.Log;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {

    public static final String URL = GFG.authUrl();

    private final SelenideElement loginButton = $("button.MuiButtonBase-root"),
     registerButton = $("a.MuiButtonBase-root"),
            usernameInput = $("input[name=username]"),
            passwordInput = $("input[name=password]"),
            submitButton = $("button.form__submit"),
            registrationLink = $(".form__link"),
            errorAlert = $(".form__error");


    @Step("Fill login page with credentials: username: {0}, password: {1}")
    @Nonnull
    public LoginPage goLoginPage() {
        loginButton.click();
        return this;
    }

    @Step("Fill login page with credentials: username: {0}, password: {1}")
    @Nonnull
    public LoginPage fillLoginPage(String login, String password) {
        setUsername(login);
        setPassword(password);
        return this;
    }

    @Step("Set username: '{0}'")
    @Nonnull
    public LoginPage setUsername(String login) {
        usernameInput.setValue(login);
        return this;
    }

    @Step("Set password: '{0}'")
    @Nonnull
    public LoginPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Submit success login")
    @Nonnull
    public FeedPage successSubmit() {
        submitButton.click();
        return new FeedPage();
    }

    @Step("Submit error login")
    @Nonnull
    public RegisterPage goRegistrationPage() {
        registrationLink.click();
        return new RegisterPage();
    }

    @Step("Submit error login")
    @Nonnull
    public RegisterPage clickButtonRegister() {
        registerButton.click();
        return new RegisterPage();
    }

    @Step("Submit error login")
    @Nonnull
    public LoginPage errorSubmit() {
        submitButton.click();
        return this;
    }

    @Step("Check alert message")
    @Nonnull
    public LoginPage checkAlertMessage(String alertMessage) {
        errorAlert.shouldHave(text(alertMessage));
        return this;
    }

    @Override
    public LoginPage checkThatPageLoaded() {
        return null;
    }
}
