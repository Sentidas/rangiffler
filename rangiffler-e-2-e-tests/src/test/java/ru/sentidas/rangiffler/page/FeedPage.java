package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Selenide.$;

public class FeedPage extends BasePage<FeedPage>{

    public static final String URL = GFG.frontUrl() + "my-travels";

    private final SelenideElement usernameInput = $("#username"),
            passwordInput = $("#password"),
            passwordSubmitInput = $("#passwordSubmit"),
            submitButton = $(".form__submit"),
            proceedLoginButton = $(".form_sign-in"),
            errorAlert = $(".form__error");


    @Step("Fill register page with credentials: username: {0}, password: {1}, submit password: {2}")
    @Nonnull
    public FeedPage fillRegisterPage(String login, String password, String passwordSubmit) {
//        setUsername(login);
//        setPassword(password);
//        setSubmitPassword(passwordSubmit);
        return this;
    }
}
