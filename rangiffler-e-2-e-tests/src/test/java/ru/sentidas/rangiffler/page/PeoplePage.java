package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.component.SearchField;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;

public class PeoplePage extends BasePage<PeoplePage> {

    public static final String URL = GFG.frontUrl() + "people";
    private final SelenideElement totalTab = $("div[aria-label='People tabs']");

    private final SearchField searchInput = new SearchField();
    private final SelenideElement peopleTable = $("#simple-tabpanel-all");
    private final SelenideElement friendTable = $("#simple-tabpanel-friends");
    private final SelenideElement outcomeTable = $("#simple-tabpanel-outcome");
    private final SelenideElement incomeTable = $("#simple-tabpanel-income");
    private final SelenideElement alertNoUser = $("p.MuiTypography-h6");
    private final SelenideElement iconNoUser = $("[data-testid=PeopleOutlineOutlinedIcon]");

    private SelenideElement currentTable;


    public PeoplePage openTab(PeopleTab tab) {
        return selectTableByTab(tab);
    }

    private PeoplePage selectTableByTab(PeopleTab tab) {
        // 1) находим сам таб по тексту
        SelenideElement button = totalTab.$$("button[role=tab]")
                .find(exactText(tab.label))
                .shouldBe(visible, enabled);
// 2) кликаем и ждём, что он стал выбранным
        button.click();
        button.shouldHave(attribute("aria-selected", "true"));

        currentTable = $("#" + tab.tableId).shouldBe(visible);
        return this;
    }


    @Override
    public PeoplePage checkThatPageLoaded() {
        totalTab.$("button").shouldHave(text("All people"));
        return this;
    }


    @Step("Send invitation to user: '{0}'")
    @Nonnull
    public PeoplePage sendFriendInvitationToUser(String username) {
        searchInput.search(username);
        SelenideElement friendRow = peopleTable.$$("tr").find(text(username));
        friendRow.$(byText("Add")).click();
        return this;
    }

    @Step("Send invitation to user: '{0}'")
    @Nonnull
    public PeoplePage acceptInvitationToUser(String username) {
        searchInput.search(username);
        SelenideElement friendRow = incomeTable.$$("tr").find(text(username));
        friendRow.$(byText("Accept")).click();
        return this;
    }

    @Step("Send invitation to user: '{0}'")
    @Nonnull
    public PeoplePage declineInvitationToUser(String username) {
        searchInput.search(username);
        SelenideElement friendRow = incomeTable.$$("tr").find(text(username));
        friendRow.$(byText("Decline")).click();
        return this;
    }

    @Step("Send invitation to user: '{0}'")
    @Nonnull
    public PeoplePage removeFriend(String username) {
        searchInput.search(username);
        SelenideElement friendRow = friendTable.$$("tr").find(text(username));
        friendRow.$(byText("Remove")).click();
        return this;
    }

    private SelenideElement rowByUsername(String username) {
        return currentTable.$("table.MuiTable-root").$("tbody").$$("tr")
                .find(text(username))
                .shouldBe(visible);
    }

    private SelenideElement rowByFirstUsername() {
        return currentTable.$("table.MuiTable-root").$("tbody").$$("tr")
                .shouldBe(sizeGreaterThan(0))
                .first();
    }

    @Step("Check invitation status for user: '{0}'")
    @Nonnull
    public PeoplePage checkInvitationSentToUser(String username) {
        searchInput.search(username);
        rowByUsername(username).shouldHave(text("Waiting..."));
        return this;
    }

    @Step("Check invitation status for user: '{0}'")
    @Nonnull
    public PeoplePage checkAcceptInvitationFromUser(String username) {
        searchInput.search(username);
        rowByUsername(username).shouldHave(text("Remove"));
        return this;
    }

    @Step("Check invitation status for user: '{0}'")
    @Nonnull
    public PeoplePage checkFriend(String username) {
        searchInput.search(username);
        rowByUsername(username).shouldHave(text("Remove"));
        return this;
    }

    @Step("Check that user with username '{0}' is present")
    @Nonnull
    public PeoplePage checkExistingUser(String username, PeopleTab tab) {
        selectTableByTab(tab);
        searchInput.search(username);
        rowByUsername(username);
        return this;
    }

    @Step("Check that user with username '{0}' is present")
    @Nonnull
    public PeoplePage checkNoExistingUser(String username) {
        searchInput.search(username);
        currentTable.$("table.MuiTable-root").$("tbody").$$("tr")
                .find(text(username))
                .shouldNot(
                        exist.because("User was not expected but is present in current tab (username='" + username + "')")

                );
        alertNoUser.should(visible);
        iconNoUser.should(visible);
        alertNoUser.shouldHave(exactText("There are no users yet"));
        return this;
    }
}
