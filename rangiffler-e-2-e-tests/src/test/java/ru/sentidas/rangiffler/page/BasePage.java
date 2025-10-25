package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;

import javax.annotation.Nonnull;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public abstract class BasePage<T extends BasePage<T>>{

    protected static final Config GFG = Config.getInstance();

    private final SelenideElement snackbarAlertRoot = $("div[role='alert']");
    private final SelenideElement snackbarAlertMessage = $("div[role='alert'] .MuiAlert-message");

    public abstract T checkThatPageLoaded();

    /**
     * Ждёт алерт и проверяет точный текст.
     * Сначала ждём появления и видимости текста, затем — exactText.
     */
    @Step("Проверка алерта с текстом: {alertMessage}")
    public T checkAlert(@Nonnull String alertMessage) {
        // CHANGED: ждём именно блок сообщения, а не контейнер Snackbar
        snackbarAlertMessage.should(appear, Duration.ofSeconds(10));

        // CHANGED: даём анимации время закончиться
        snackbarAlertRoot.shouldBe(visible, Duration.ofSeconds(5));
        snackbarAlertMessage.shouldBe(visible, Duration.ofSeconds(5));

        // ADDED: подстраховка на финальное состояние анимации (opacity: 1)
        snackbarAlertRoot.shouldHave(Condition.cssValue("opacity", "1"), Duration.ofSeconds(5));

        // CHANGED: exactText отдельной фазой — понятнее лог
        snackbarAlertMessage.shouldHave(exactText(alertMessage), Duration.ofSeconds(5));
        return self();
    }

    // ADDED: безопасный self без unchecked cast в каждом методе
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
