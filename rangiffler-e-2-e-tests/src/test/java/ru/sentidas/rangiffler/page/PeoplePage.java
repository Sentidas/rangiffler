package ru.sentidas.rangiffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.component.SearchField;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class PeoplePage extends BasePage<PeoplePage>{

    public static final String URL = GFG.frontUrl() + "people";
    private final SelenideElement totalTab = $("div[aria-label='People tabs']");

    private final SearchField searchInput = new SearchField();
    private final SelenideElement peopleTable = $("#simple-tabpanel-all");
    private final SelenideElement friendTable = $("#all");
    private final SelenideElement alertNoUser = $("p.MuiTypography-h6");
    private final SelenideElement iconNoUser = $("[data-testid=PeopleOutlineOutlinedIcon]");

  public PeoplePage openTab(PeopleTab tab) {
      return openTabByText(tab.label);
  }

    private PeoplePage openTabByText(String label) {
        // 1) находим сам таб по тексту
        SelenideElement tab = totalTab.$$("button[role=tab]")
                .find(exactText(label))
                .shouldBe(visible, enabled);
// 2) кликаем и ждём, что он стал выбранным
        tab.click();
        tab.shouldHave(attribute("aria-selected", "true"));
        SelenideElement  activePanel = $$("[role=tabpanel]").find(visible).shouldBe(visible);

        return this;
  }


    @Override
    public PeoplePage checkThatPageLoaded() {
        totalTab.$("button").shouldHave(text("All people"));
        return this;
    }

    public PeoplePage selectAllUsers() throws InterruptedException {
        totalTab.$("button").shouldHave(text("All people")).click();
        Thread.sleep(3000);
        return this;
    }

    public PeoplePage selectOutcomeInvitation() {
        totalTab.$("button").shouldHave(text("Outcome invitations")).click();
        return this;
    }

    public PeoplePage selectIncomeInvitation() {
        totalTab.$("button").shouldHave(text("Income invitations")).click();
        return this;
    }

    public PeoplePage selectFriends() {
        totalTab.$("button").shouldHave(text("Friends")).click();
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

    public String sendFriendInvitationToFirstUser() {
        // FIX: берём первую строку из tbody
        SelenideElement row = peopleTable.$(".MuiTable-root")
                .$("tbody").$$("tr").first()
                .shouldBe(visible);

        // username в первой текстовой ячейке <td>
        String username = row.$$("td").first().getText();

        // FIX: клик строго по кнопке
        row.$("button").shouldHave(text("Add")).shouldBe(visible, enabled).click();

        // можно сразу дождаться "Waiting..." в этой строке
        row.shouldHave(text("Waiting..."));
        return username;
    }

    @Step("Check invitation status for user: '{0}'")
    @Nonnull
    public PeoplePage checkInvitationSentToUser(String username) {
       // searchInput.search(username);
        SelenideElement friendRow = peopleTable.$$("tr").find(text(username));
        friendRow.shouldHave(text("Waiting..."));
        return this;
    }


    @Step("Check that user with username '{0}' is present")
    @Nonnull
    public PeoplePage checkExistingUser(String username) {
        searchInput.search(username);
        peopleTable.$$("tr").find(text(username)).should(visible);
        return this;
    }

    @Step("Check that user with username '{0}' is present")
    @Nonnull
    public PeoplePage checkNoExistingUser(String username) throws InterruptedException {
        searchInput.search(username);
        alertNoUser.should(visible);
        iconNoUser.should(visible);
        alertNoUser.shouldHave(exactText("T7here are no users yet"));
        Thread.sleep(3000);
        return this;
    }
}
