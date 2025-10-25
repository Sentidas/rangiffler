package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class SearchField extends BaseComponent<SearchField> {

  public SearchField(@Nonnull SelenideElement self) {
    super(self);
  }
  public SearchField() {
    super($("input[aria-label='search people']"));
  }


  @Nonnull
  @Step("Search data in table by value '{0}'")
  public SearchField search(String query) {
   clearIfNotEmpty();
    self.setValue(query).pressEnter();
    return this;
  }

  @Nonnull
  @Step("Clear search field")
  public SearchField clearIfNotEmpty() {
    if (self.is(not(empty))) {
        String os = System.getProperty("os.name").toLowerCase();
        String selectAllChord = os.contains("mac") ? Keys.chord(Keys.COMMAND, "a")
                : Keys.chord(Keys.CONTROL, "a");
        self.sendKeys(selectAllChord);
        self.sendKeys(Keys.BACK_SPACE);
      self.should(empty);
    }
    return this;
  }
}
