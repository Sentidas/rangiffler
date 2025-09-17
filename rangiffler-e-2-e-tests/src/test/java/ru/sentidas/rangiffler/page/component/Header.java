package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.*;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class Header extends BaseComponent<Header> {

  public Header() {
    super($("#root header"));
  }

  private final SelenideElement feedPageLink = self.$("a[href*='/my-travels']");
  private final SelenideElement exitBtn = self.$("button[aria-label=Logout]");
  private final SelenideElement menuBtn = self.$("button[aria-label=open drawer]");
  private final SelenideElement iconProfile = $("[data-testid=AccountCircleRoundedIcon]");
  private final SelenideElement iconFeed= $("[data-testid=PublicRoundedIcon]");
  private final SelenideElement iconPeople = $("[data-testid=PersonSearchRoundedIcon]");
//  private final ElementsCollection menuItems = menu.$$("li");

  @Step("Open People page")
  @Nonnull
  public PeoplePage toPeoplePage() {
    iconPeople.click();
    return new PeoplePage();
  }

  @Step("Open Profile page")
  @Nonnull
  public ProfilePage toProfilePage() {
    iconProfile.click();
    return new ProfilePage();
  }

  @Step("Open Profile page")
  @Nonnull
  public FeedPage toFeedPage() {
    iconFeed.click();
    return new FeedPage();
  }

  @Step("Sign out")
  @Nonnull
  public LoginPage signOut() {
    exitBtn.click();
    return new LoginPage();
  }



  @Step("Go to main page")
  @Nonnull
  public FeedPage toFeedPageWithClickHeader() {
    feedPageLink.click();
    return new FeedPage();
  }
}
