package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import ru.sentidas.rangiffler.page.FeedPage;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class EditPhoto extends BaseComponent<EditPhoto> {

    protected EditPhoto(SelenideElement self) {
        super(self);
    }

    public EditPhoto() {
        super($("div[role=dialog]"));
    }


    private final SelectField countrySelect = new SelectField($("#country"));

    private final SelenideElement header = self.$("h2.MuiDialogTitle-root");
    private final SelenideElement photoInput = self.$("input[type='file']");
    private final SelenideElement descriptionInput = self.$("#description");
    private final SelenideElement saveBtn = self.$("button[type=submit]");
    private final SelenideElement cancelBtn = self.$("button[type=button]");


    @Nonnull
    public EditPhoto checkThatComponentLoaded() {
        header.should(visible).shouldHave(text("Edit photo"));
        return this;
    }

    @Step("Select new photo country: '{0}'")
    @Nonnull
    public EditPhoto uploadNewImage(String path) {
        photoInput.uploadFromClasspath(path);
        return this;
    }


    @Step("Select new photo country: '{0}'")
    @Nonnull
    public EditPhoto setNewCountry(String countryName) {
        countrySelect.setValue(countryName);
        return this;
    }

    @Step("Set new photo description: '{0}'")
    @Nonnull
    public EditPhoto setPhotoDescription(String description) {
        String os = System.getProperty("os.name").toLowerCase();
        String selectAllChord = os.contains("mac") ? Keys.chord(Keys.COMMAND, "a")
                : Keys.chord(Keys.CONTROL, "a");

        descriptionInput.shouldBe(visible, enabled)
                .scrollIntoView(true)
                .click();
        descriptionInput.sendKeys(selectAllChord);
        descriptionInput.sendKeys(Keys.BACK_SPACE);

        descriptionInput.setValue(description);
        descriptionInput.shouldHave(value(description), Duration.ofSeconds(5));
        return this;
    }

    @Step("Click submit button to create photo")
    @Nonnull
    public FeedPage save() {
        saveBtn.click();
        return new FeedPage();
    }

    @Step("Click submit button to edit photo")
    @Nonnull
    public EditPhoto cancel() {
        cancelBtn.click();
        return this;
    }
}
