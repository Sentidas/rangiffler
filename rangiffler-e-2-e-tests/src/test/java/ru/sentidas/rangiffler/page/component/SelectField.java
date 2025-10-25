package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@ParametersAreNonnullByDefault
public class SelectField extends BaseComponent<SelectField> {

  public SelectField(SelenideElement self) {
    super(self);
  }

  private final SelenideElement optionsMenu = $("ul[role='listbox']");
  private final ElementsCollection optionItems = optionsMenu.$$("[role='option']");

  @Step("Pick value '{0}' from select component ")
  public void setValue(String value) {
    self.shouldBe(visible, enabled).click();
    optionsMenu.should(appear);

    optionItems.find(text(value))
            .shouldBe(visible, enabled)
            .click();
    optionsMenu.should(disappear);                      // дождаться закрытия

  }

  @Step("Pick value by code '{0}' from select component ")
  public void setValueByCode(String code) {
    self.shouldBe(visible, enabled).click();
    optionsMenu.should(appear);

    optionsMenu.$("li[role='option'][data-value='" + code + "']")
            .shouldBe(visible, enabled)
            .click();

    optionsMenu.should(disappear);
  }

  @Step("Check that selected value is equal to '{0}'")
  public void checkSelectValueIsEqualTo(String value) {
    self.shouldHave(text(value));
  }
}
