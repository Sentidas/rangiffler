package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends BasePage<T>>{

    protected static final Config GFG = Config.getInstance();

    private SelenideElement alert = $("div.MuiSnackbar-root");


    public abstract T checkThatPageLoaded() ;

    @Step("Check that alert message appears: {alertMessage}")
    @Nonnull
    public T checkAlert(String alertMessage) {
        alert.should(visible).should(text(alertMessage));
        return (T) this;
    }
}
