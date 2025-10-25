package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import ru.sentidas.rangiffler.page.component.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static ru.sentidas.rangiffler.condition.PhotoConditions.hasCountryAndDescription;
import static ru.sentidas.rangiffler.condition.ScreenshotConditions.image;

public class FeedPage extends BasePage<FeedPage> {

    public static final String URL = GFG.frontUrl() + "my-travels";

    private final Header header = new Header();
    private final Pagination pagination = new Pagination($x("//div[button[text()='Previous'] and button[text()='Next']]"));
    private final Map map = new Map($("figure.worldmap__figure-container"));
    private final SelenideElement mapLocator = $("figure.worldmap__figure-container");

    @Nonnull
    public Header getHeader() {
        return header;
    }

    @Nonnull
    public Pagination pagination() {
        return pagination;
    }

    @Nonnull
    public Map map() {
        return map;
    }

    @Step("Select new photo country: '{0}'")
    @Nonnull
    public FeedPage checkMap(BufferedImage expected) {
        mapLocator.scrollIntoView("{block:'center', inline:'center'}")
                .shouldBe(visible);
        map.waitMapReady();
        mapLocator.shouldHave(image(expected));

        return this;
    }

    public boolean isMyActive() {
        String pressed = $("[value='my']").getAttribute("aria-pressed");
        return "true".equals(pressed);
    }

    private final ElementsCollection buttons = $$("button.MuiButtonBase-root");
    private final ElementsCollection cards = $$("div.MuiPaper-elevation3");
    private final SelenideElement loader = $("svg.MuiCircularProgress-svg");
    private final SelenideElement cardsContainer = $("div.MuiGrid-container");
    private final SelenideElement withFriendsBtn = $("button[value=friends]");
    private final SelenideElement onlyMysBtn = $("button[value=my]");

    private SelenideElement nextBtn() {
        return buttons.find(text("Next"));
    }

    private SelenideElement previousBtn() {
        return buttons.find(text("Previous"));
    }


    public FeedPage shouldHaveNoPagination() {
        pagination.shouldBe(Pagination.Prev.HIDDEN, Pagination.Next.HIDDEN);
        return this;
    }

    public FeedPage shouldHavePagination(Pagination.Prev prevStat, Pagination.Next nextStat) {
        pagination.shouldBe(prevStat, nextStat);
        return this;
    }

    public FeedPage addPhoto(String path, String countryName, @Nullable String description) {
        CreatePhoto dialog = addPhoto();
        dialog.uploadNewImage(path)
                .setNewCountry(countryName);

        if (description != null) {
            dialog.setPhotoDescription(description);
        }
        return dialog.save();
    }

    public FeedPage addPhoto(String path, String code) {
        CreatePhoto dialog = addPhoto();
        dialog.uploadNewImage(path)
                .setNewCountry(code);

        return dialog.save();
    }


    public void waitLoadingFinished() {
        if (loader.exists()) {
            loader.should(disappear, Duration.ofSeconds(7));
        }
        cardsContainer.should(exist, Duration.ofSeconds(10));
    }

    @Step("Add new photo")
    @Nonnull
    public CreatePhoto addPhoto() {
        SelenideElement btn = buttons.find(text("Add photo"))
                .shouldBe(visible, enabled);
        // чтобы AppBar не перекрывал: прокрутить в центр вьюпорта
        btn.scrollIntoView("{behavior: \"instant\", block: \"center\", inline: \"center\"}");
        btn.click();
        return new CreatePhoto();
    }

    @Step("openFriendsFeed")
    @Nonnull
    public FeedPage openFriendsFeed() {
        withFriendsBtn.shouldBe(visible, Duration.ofSeconds(7)).click();
        return this;
    }

    @Step("like photo")
    @Nonnull
    public FeedPage likePhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$("button[aria-label=like]")
                .shouldBe(visible, enabled)
                .click();
        return this;
    }

    @Step("checkUnSuccessLikesPhoto")
    @Nonnull
    public FeedPage checkUnSuccessLikesPhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .should(text("0 likes"));

        return this;
    }

    @Step("checkAllLikes")
    public int checkCountLikes(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        String text = card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .getText();

        int count = Integer.parseInt(text.replace(" likes", "").trim());

        return count;
    }


    @Step("checkSuccessLikesPhoto")
    @Nonnull
    public FeedPage checkSuccessLikesPhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled)
                .should(text("1 likes"));

        return this;
    }

    public FeedPage assertCardNotLiked(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled);

        card.$("button[aria-label='like'] svg[data-testid=FavoriteBorderOutlinedIcon]").should(exist);
        card.$("button[aria-label='like'] svg[data-testid=FavoriteOutlinedIcon]").shouldNot(exist);
        return this;
    }

    public FeedPage assertCardLiked(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$("p.MuiTypography-root")
                .shouldBe(visible, enabled);

        card.$("button[aria-label='like'] svg[data-testid=FavoriteBorderOutlinedIcon]").shouldNot(exist);
        card.$("button[aria-label='like'] svg[data-testid=FavoriteOutlinedIcon]").should(exist);
        return this;
    }

    @Step("Open my feed")
    @Nonnull
    public FeedPage openMyFeed() {
        onlyMysBtn.shouldBe(visible, Duration.ofSeconds(7)).click();
        return this;
    }

    public EditPhoto editPhoto(String countryName, @Nullable String description) {
        cards.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        ElementsCollection matches = cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15));

        SelenideElement card = matches.first().shouldBe(visible);
        card.$$("button.MuiButtonBase-root")
                .findBy(exactText("Edit"))
                .shouldBe(visible, enabled)
                .click();

        return new EditPhoto().checkThatComponentLoaded();
    }

    public FeedPage deletePhoto(String countryName, String description) {
        ElementsCollection target = cards.filterBy(hasCountryAndDescription(countryName, description));
        target.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15)); // дождались, что цель есть

        target.first().$$("button.MuiButtonBase-root")
                .findBy(text("Delete"))
                .shouldBe(enabled)
                .click();

        waitLoadingFinished();

        return this;
    }

    public FeedPage deletePhotoByNumber(int number) {
        ElementsCollection target = cards;
        target.shouldHave(sizeGreaterThan(0), Duration.ofSeconds(15)); // дождались, что цель есть

        target.get(number - 1).$$("button.MuiButtonBase-root")
                .findBy(text("Delete"))
                .shouldBe(enabled)
                .click();
        return this;
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
        cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(
                        sizeGreaterThan(0).because(
                                "Post was expected but not found 'country = " +
                                        countryName + "', description = " + description + "'"
                        ),
                        Duration.ofSeconds(7)
                );
        return this;
    }

    @Step("Click submit button to create new spending")
    @Nonnull
    public FeedPage checkThatPostFirst(@Nonnull String countryName, @Nullable String description) {
        goFirstPage();
        SelenideElement firstPost = cardsContainer.$$("div.MuiPaper-elevation3").first();
        firstPost.$("h3").shouldHave(text(countryName));
        firstPost.$(".photo-card__content").shouldHave(text(description));
        return this;
    }

    private void goFirstPage() {
        SelenideElement previous = previousBtn();
        if (previous.exists() && isEffectivelyEnabled(previous)) {
            previous.click();
        }
    }

    private boolean isEffectivelyEnabled(SelenideElement btn) {
        btn.shouldBe(visible);
        boolean enabled = btn.isEnabled();
        boolean muiDisabled = btn.has(cssClass("Mui-disabled"));

        return enabled && !muiDisabled;
    }

    @Step("Assert card is absent (country + optional description)")
    @Nonnull
    public FeedPage checkNotExistPost(@Nonnull String countryName, @Nullable String description) {
        cards.filterBy(hasCountryAndDescription(countryName, description))
                .shouldHave(CollectionCondition.size(0).because(
                        "Post was not expected but is still present " +
                                "(country='" + countryName + "', description='" + description + "')"
                ), Duration.ofSeconds(7));
        return this;
    }
}
