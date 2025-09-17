package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.component.SearchField;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class FriendsPage extends BasePage<FriendsPage>{

    public static final String URL = GFG.frontUrl() + "people";
    private final SelenideElement totalTab = $("div[aria-label='People tabs']");

    private final SearchField searchInput = new SearchField();
    private final SelenideElement peopleTable = $("#simple-tabpanel-all");
    private final SelenideElement friendTable = $("#all");




    @Override
    public FriendsPage checkThatPageLoaded() {
        totalTab.$("button").shouldHave(text("All people"));
        return this;
    }

    public FriendsPage selectAllUsers() {
        totalTab.$("button").shouldHave(text("All people")).click();
        return this;
    }

    public FriendsPage selectOutcomeInvitation() {
        totalTab.$("button").shouldHave(text("Outcome invitations")).click();
        return this;
    }

    public FriendsPage selectIncomeInvitation() {
        totalTab.$("button").shouldHave(text("Income invitations")).click();
        return this;
    }

    public FriendsPage selectFriends() {
        totalTab.$("button").shouldHave(text("Friends")).click();
        return this;
    }

    @Step("Send invitation to user: '{0}'")
    @Nonnull
    public FriendsPage sendFriendInvitationToUser(String username) {
        searchInput.search(username);
        SelenideElement friendRow = peopleTable.$$("tr").find(text(username));
        friendRow.$(byText("Add")).click();
        return this;
    }

    @Step("Check invitation status for user: '{0}'")
    @Nonnull
    public FriendsPage checkInvitationSentToUser(String username) {
        searchInput.search(username);
        SelenideElement friendRow = peopleTable.$$("tr").find(text(username));
        friendRow.shouldHave(text("Waiting..."));
        return this;
    }

    @Step("Check that user with username '{0}' is present")
    @Nonnull
    public FriendsPage checkExistingUser(String username) {
        searchInput.search(username);
        peopleTable.$$("tr").find(text(username)).should(visible);
        return this;
    }
}
