package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.component.SelectField;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static ru.sentidas.rangiffler.condition.ScreenshotConditions.image;

public class ProfilePage extends BasePage<ProfilePage> {


    public static final String URL = GFG.frontUrl() + "profile";

    private final SelenideElement header = $(".MuiContainer-root h2");
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement firstnameInput = $("#firstname");
    private final SelenideElement surnameInput = $("#surname");
    private final SelenideElement avatarInput = $(".MuiContainer-root input[type='file']");
    private final SelenideElement avatar = $("#image__input").parent().$("img");

    private final SelectField countrySelect = new SelectField($("#location"));
    private final SelenideElement countryInput = $("#location");
    private final SelenideElement saveBtn = $("button[type=submit]");
    private final SelenideElement resetBtn = $(".MuiContainer-root button[type=button]");


    public ProfilePage checkUsername(String expectedUsername) {
        usernameInput.shouldHave(value(expectedUsername));
        usernameInput.shouldHave(disabled);   // 2) элемент задизейблен
        usernameInput.shouldHave(Condition.attribute("disabled"));// 4) (дополнительно) у инпута присутствует атрибут disabled
        return this;
    }

    public ProfilePage checkFirstname(String expectedFirstname) {
        firstnameInput.shouldHave(value(expectedFirstname));
        firstnameInput.shouldHave(enabled);   // 2) элемент  не задизейблен
        return this;
    }

    public ProfilePage checkSurname(String expectedSurname) {
        surnameInput.shouldHave(value(expectedSurname));
        surnameInput.shouldHave(enabled);   // 2) элемент задизейблен
        return this;
    }

    public ProfilePage checkLocation(String expectedLocation) {
        countryInput.shouldHave(text(expectedLocation));
        return this;
    }

    @Step("Upload avatar from classpath")
    @Nonnull
    public ProfilePage uploadPhotoFromClasspath(String path) {
        avatarInput.uploadFromClasspath(path);
        return this;
    }

    public ProfilePage setFirstname(String firstname) {
        firstnameInput.clear();
        firstnameInput.setValue(firstname);
        return this;
    }

    public ProfilePage setSurname(String expectedSurname) {
        surnameInput.clear();
        surnameInput.setValue(expectedSurname);
        return this;
    }

    @Step("Select new photo country: '{0}'")
    @Nonnull
    public ProfilePage setNewCountry(String countryName) {
        countrySelect.setValue(countryName);
        return this;
    }


    @Step("Select new photo country: '{0}'")
    @Nonnull
    public ProfilePage checkAvatar(BufferedImage expected) {
        avatar.shouldHave(image(expected));
        return this;
    }

    @Step("Select new photo country: '{0}'")
    @Nonnull
    public ProfilePage checkAvatarExist() {
        avatar.should(attributeMatching("src", "data:image.*"));
        return this;
    }

    @Step("Click submit button to edit photo")
    @Nonnull
    public ProfilePage save() {
        saveBtn.click();
        return this;
    }

    @Step("Click submit button to edit photo")
    @Nonnull
    public ProfilePage reset() {
        resetBtn.shouldHave(text("Reset")).click();
        return this;
    }

    @Override
    @Step
    @Nonnull
    public ProfilePage checkThatPageLoaded() {
        header.shouldHave(text("My profile"));
        return this;
    }
}
