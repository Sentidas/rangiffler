package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.FeedPage;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$x;

@ParametersAreNonnullByDefault
public class Pagination extends BaseComponent<Pagination> {

    public Pagination(SelenideElement self) {
        super(self);
    }

    public Pagination() {
        super($x("//div[button[text()='Previous'] and button[text()='Next']]"));
    }


    private final SelenideElement next = self.$x("./button[text() = 'Next']");
    private final SelenideElement previous = self.$x("./button[text() = 'Previous']");

    public enum Prev {HIDDEN, DISABLE, ENABLE}

    public enum Next {HIDDEN, DISABLE, ENABLE}

    @Step("Pagination should be: prev={0}, next={1}")
    public void shouldBe(Prev prevState, Next nextState) {
        // Случай "нет пагинации": обе кнопки отсутствуют, контейнера тоже нет — это норма.
        if (prevState == Prev.HIDDEN && nextState == Next.HIDDEN) {
            $$("button").findBy(exactText("Previous")).shouldNot(exist);
            $$("button").findBy(exactText("Next")).shouldNot(exist);
            return;
        }
        switch (prevState) {
            case HIDDEN -> previous.shouldNot(exist);
            case DISABLE -> previous.shouldBe(visible).shouldNotBe(enabled);
            case ENABLE -> previous.shouldBe(visible).shouldBe(enabled);
        }

        switch (nextState) {
            case HIDDEN -> next.shouldNot(exist);
            case DISABLE -> next.shouldBe(visible).shouldNotBe(enabled);
            case ENABLE -> next.shouldBe(visible).shouldBe(enabled);
        }
    }

    @Step("Pagination: click Next")
    public Pagination clickNext() {
        next.shouldBe(visible, enabled).click();
        // 🔽 дождёмся, что лента перерисовалась
        new FeedPage().waitLoadingFinished();
        return this;
    }

    @Step("Pagination: click Previous")
    public Pagination clickPrevious() {
        previous.shouldBe(visible, enabled).click();
        // 🔽 дождёмся, что лента перерисовалась
        new FeedPage().waitLoadingFinished();
        return this;
    }

    public boolean isEffectivelyEnabled(SelenideElement btn) {
        return btn.isEnabled() && btn.has(cssClass("Mui-disabled"));

    }

    @Step("Pagination: try click Previous (no-op if disabled)")
    public Pagination tryClickPrevious() {
        previous.shouldBe(visible);
        if (!isEffectivelyEnabled(previous)) {
            return this;
        }
        previous.click();
        new FeedPage().waitLoadingFinished();
        return this;
    }

    @Step("Pagination: try click Previous (no-op if disabled)")
    public Pagination tryClickNext() {
        next.shouldBe(visible);
        if (!isEffectivelyEnabled(next)) {
            return this;
        }
        next.click();
        new FeedPage().waitLoadingFinished();
        return this;
    }
}
