package ru.sentidas.rangiffler.condition;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Driver;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebElementCondition;
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public final class PhotoConditions {

    private PhotoConditions() {}
    /**
     * Поиск фото h3 == countryName И p.photo-card__content == description
     */
    public static WebElementCondition hasCountryAndDescription(@Nonnull String countryName,
                                                               @Nullable String description) {
        return new WebElementCondition("h3==country AND p.photo-card__content==desc") {
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                SelenideElement card = $(element);

                // название страны (<h3>)
                String countryHeader = card.$("h3").text().trim();

                // описание фото (<p.photo-card__content>) может отсутствовать
                SelenideElement descriptionElement = card.$("p.photo-card__content");
                boolean descriptionElementExists = descriptionElement.exists();
                String descriptionText = descriptionElementExists ? descriptionElement.text().trim() : null;

                boolean matches;
                if (description == null) {
                    matches = countryHeader.equals(countryName)
                            && (descriptionText == null || descriptionText.isEmpty());

                } else {
                    matches = countryHeader.equals(countryName) && descriptionText.equals(description);
                }

                String actualValueForError =
                        "h3='" + countryHeader + "', p=" +
                                (descriptionText == null ? "<absent>" : "'" + descriptionText + "'");
                return new CheckResult(matches, actualValueForError);
            }
        };
    }
}
