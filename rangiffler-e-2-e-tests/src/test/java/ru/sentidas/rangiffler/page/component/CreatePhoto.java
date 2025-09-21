package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.page.FeedPage;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@ParametersAreNonnullByDefault
public class CreatePhoto extends BaseComponent<CreatePhoto> {

  protected CreatePhoto(SelenideElement self) {
    super(self);
  }

  public CreatePhoto() {
    super($("div[role=dialog]"));
  }


  private final SelectField countrySelect = new SelectField($("#country"));

  private final SelenideElement header = self.$("h2.MuiDialogTitle-root");
  private final SelenideElement photoInput = self.$("input[type='file']");
  private final SelenideElement descriptionInput = self.$("#description");
  private final SelenideElement saveBtn = self.$("button[type=submit]");
  private final SelenideElement cancelBtn = self.$("button[type=button]");
  private final SelenideElement photoContent = self.$("div.MuiDialogContent-root");


  @Nonnull
  public CreatePhoto checkThatComponentLoaded() {
    header.should(visible).shouldHave(text("Add photo"));
    return this;
  }



  @Step("Select new photo country: '{0}'")
  @Nonnull
  public CreatePhoto uploadNewImage(String path) {
    photoInput.uploadFromClasspath(path);
    return this;
  }


  @Step("Select new photo country: '{0}'")
  @Nonnull
  public CreatePhoto setNewCountry(String countryName) {
    countrySelect.setValue(countryName);
    return this;
  }

  @Step("Set new photo description: '{0}'")
  @Nonnull
  public CreatePhoto setPhotoDescription(String description) {
    descriptionInput.clear();
    descriptionInput.setValue(description);
    return this;
  }

  @Step("Click submit button to create new spending")
  @Nonnull
  public FeedPage savePhoto() {
    saveBtn.shouldBe(visible, enabled).click();
    getSelf().should(disappear, Duration.ofSeconds(10));
    $("div.MuiSnackbar-root").shouldBe(visible, Duration.ofSeconds(10));
    return new FeedPage();
  }

  @Step("Click submit button to create new spending")
  @Nonnull
  public CreatePhoto checkErrorMessage(String alert) {
    photoContent.shouldHave(text(alert));
    return this;
  }


}
