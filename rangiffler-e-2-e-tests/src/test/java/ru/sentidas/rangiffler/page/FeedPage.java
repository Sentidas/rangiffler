package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.*;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import ru.sentidas.rangiffler.page.component.CreatePhoto;
import ru.sentidas.rangiffler.page.component.EditPhoto;
import ru.sentidas.rangiffler.page.component.Header;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class FeedPage extends BasePage<FeedPage> {

    public static final String URL = GFG.frontUrl() + "my-travels";
    private final CreatePhoto editPhoto = new CreatePhoto();

    protected final Header header = new Header();

    @Nonnull
    public Header getHeader() {
        return header;
    }

    private final ElementsCollection buttons = $$("button.MuiButtonBase-root");
    private final ElementsCollection cards = $$("div.MuiPaper-elevation3");
    private final ElementsCollection buttonsInCards = $$("div.MuiPaper-elevation3 button.MuiButtonBase-root");
    private final ElementsCollection countiesPost = $$("div.MuiPaper-elevation3 h3");
    private final ElementsCollection descriptionsPost = $$("div.MuiPaper-elevation3 div.MuiPaper-elevation3 p.photo-card__content");

    private final SelenideElement withFriendsBtn = $("button[value=friends]");
    private final SelenideElement onlyMysBtn = $("button[value=my]");
    private final SelenideElement likeBtn = $("button[label=like]");
    private final SelenideElement likeIcon = $("data-testid=FavoriteOutlinedIcon");
    // div.MuiPaper-elevation3
    @Step("Add new photo")
    @Nonnull
    public CreatePhoto addPhoto() {
        buttons.find(text("Add photo")).click();
        return new CreatePhoto();
    }

    @Step("Edit photo")
    @Nonnull
    public EditPhoto editFirstPhoto() {
        cards.find((text("Edit"))).click();
        return new EditPhoto();
    }

    @Step("Edit photo")
    @Nonnull
    public FeedPage clickFeedWithFriend() {
        withFriendsBtn.click();
        return this;
    }

    @Step("Edit photo")
    @Nonnull
    public FeedPage likePhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$("button[aria-label=like]")
                .shouldBe(visible, enabled)
                .click();
        return this;
    }

    @Step("Edit photo")
    @Nonnull
    public FeedPage checkUnSuccessLikesPhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .should(text("0 likes"));

        return this;
    }

    @Step("Edit photo")
    @Nonnull
    public String checkAllLikes(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        String text = card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .getText();

        return text;
    }


@Step("Edit photo")
    @Nonnull
    public FeedPage checkSuccessLikesPhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .should(text("1 likes"));

        return this;
    }

    public FeedPage assertCardNotLiked(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled);

        card.$("button[aria-label='like'] svg[data-testid=FavoriteBorderOutlinedIcon]").should(exist);
        card.$("button[aria-label='like'] svg[data-testid=FavoriteOutlinedIcon]").shouldNot(exist);
        return this;
    }

    public FeedPage assertCardLiked(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled);

        card.$("button[aria-label='like'] svg[data-testid=FavoriteBorderOutlinedIcon]").shouldNot(exist);
        card.$("button[aria-label='like'] svg[data-testid=FavoriteOutlinedIcon]").should(exist);
        return this;
    }

    @Step("Edit photo")
    @Nonnull
    public FeedPage clickMyTravels() {
        onlyMysBtn.click();
        return this;
    }

    /** Кастомный предикат: в одной карточке h3 == countryName И p.photo-card__content == description */
    private WebElementCondition cardHas(@Nonnull String countryName, @Nullable String description) {
        return new WebElementCondition("h3==country AND p.photo-card__content==desc") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                SelenideElement card = $(element);

                // название страны (<h3>)
                String countryHeader = card.$("h3").text().trim();

                // Описание (<p.photo-card__content>) может отсутствовать
                SelenideElement descriptionElement = card.$("p.photo-card__content");
                boolean descriptionElementExists = descriptionElement.exists();
                String descriptionText = descriptionElementExists ? descriptionElement.text().trim() : null;


                boolean matches;
                if (description == null) {
                    // Ожидаем, что описания нет или оно пустое
                    matches = countryHeader.equals(countryName) && (descriptionText == null || descriptionText.isEmpty());

                } else {
                    // Ожидаем точное совпадение текста описания
                    matches = countryHeader.equals(countryName) && descriptionText.equals(description);
                }
                // actualValue попадёт в сообщение об ошибке, если условие не выполнится
                String actualValueForError =
                        "h3='" + countryHeader + "', p=" +
                                (descriptionText == null ? "<absent>" : "'" + descriptionText + "'");
                return new CheckResult(matches, actualValueForError);
            }
        };
    }

    public EditPhoto editPhoto(String countryName, @Nullable String description) {
        // 1) Ждём, что на странице появились карточки вообще
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 2) Ждём, что среди них есть нужная (country + optional description)
        ElementsCollection matches = cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        // 3) Берём первую подходящую карточку и кликаем Edit
        SelenideElement card = matches.first().shouldBe(visible);
        card.$$("button.MuiButtonBase-root")
                .findBy(exactText("Edit"))
                .shouldBe(visible, enabled)
                .click();

        // 4) Дождёмся, что модалка реально открылась
        return new EditPhoto().checkThatComponentLoaded();
    }
//
//    /** Использование: редактировать карточку по паре (страна + описание) */
//    public EditPhoto editPhoto(String countryName, String description) {
//        cards.findBy(cardHas(countryName, description))
//              //  .should(exist, Duration.ofSeconds(15))
//                .should(visible, Duration.ofSeconds(15))
//                .$$("button.MuiButtonBase-root")
//                .findBy(text("Edit"))
//                .click();
//        return new EditPhoto();
//    }



    @Step("Edit photo")
    @Nonnull
    public FeedPage deleteFirstPhoto() {
        cards.find(text("Delete")).click();
        return new FeedPage();
    }



    /** Использование: удалить карточку по паре (страна + описание) */
    public void deletePhoto(String countryName, String description) {
        ElementsCollection target = cards.filterBy(cardHas(countryName, description));
        target.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15)); // дождались, что цель есть

        target.first().$$("button.MuiButtonBase-root")
                .findBy(text("Delete"))
                .shouldBe(enabled)
                .click();
    }

    @Step("Check that page is loaded")
    @Nonnull
    public FeedPage checkThatPageLoaded() {
        System.out.println("успешная загрузка страницы my_travel");
       // header.getSelf().should(visible).shouldHave(text("angiffler"));
//        statComponent.getSelf().should(visible).shouldHave(text("Statistics"));
//        spendingTable.getSelf().should(visible).shouldHave(text("History of Spendings"));
        return this;
    }

    @Step("Click submit button to create new spending")
    @Nonnull
    public FeedPage checkExistPost(@Nonnull String countryName, @Nullable String description) {
        cards.filterBy(cardHas(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));
        return this;
    }

    @Step("Assert card is absent (country + optional description)")
    @Nonnull
    public FeedPage checkNotExistPost(@Nonnull String countryName, @Nullable String description) {
        cards.filterBy(cardHas(countryName, description))
                .shouldHave(CollectionCondition.size(0));
        return this;
    }
}
