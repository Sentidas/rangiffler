package ru.sentidas.rangiffler.page.component;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.be;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.actions;

@ParametersAreNonnullByDefault
public class Map extends BaseComponent<Map> {

    private final SelenideElement tooltip = self.$("svg g[pointer-events='none'] text tspan");

    public Map(@Nonnull SelenideElement self) {
        super(self);
    }

    public void expectedTooltip(String countryName, int count) {
        waitMapReady();

        final String expected = (countryName + " " + count).trim().replaceAll("\\s+", " ");
        final String selector = "svg g[pointer-events='none'] text tspan";

        final long deadline = System.currentTimeMillis() + 8000; // терпеливее к перерисовкам

        boolean seenAny = false;

        while (true) {
            ElementsCollection tips = self.$$(selector);

            if (!tips.isEmpty()) {
                seenAny = true;
                for (SelenideElement tip : tips) {
                    String text = tooltipText(tip);
                    if (expected.equals(text)) {
                        return;
                    }
                }
            }

            if (System.currentTimeMillis() > deadline) {
                java.util.List<String> snapshot = new java.util.ArrayList<>();
                for (SelenideElement tip : self.$$(selector)) {
                    snapshot.add(tooltipText(tip));
                }

                String msg = seenAny
                        ? "Не нашли тултип \"" + expected + "\". В DOM: " + snapshot
                        : "Не дождались появления ни одного тултипа. В DOM: " + snapshot;
                throw new AssertionError(msg);
            }

            com.codeborne.selenide.Selenide.sleep(100);
        }
    }

    public void absentTooltip(String countryName) {
        waitMapReady();

        final String selector = "svg g[pointer-events='none'] text tspan";
        final long deadline = System.currentTimeMillis() + 8000;

        while (true) {
            boolean present = false;

            ElementsCollection tips = self.$$(selector);
            if (!tips.isEmpty()) {
                for (SelenideElement tip : tips) {
                    String text = tooltipText(tip);
                    if (text.equals(countryName) || text.startsWith(countryName + " ")) {
                        present = true;
                        break;
                    }
                }
            }

            if (!present) {
                return;
            }

            if (System.currentTimeMillis() > deadline) {
                java.util.List<String> snapshot = new java.util.ArrayList<>();
                for (SelenideElement tip : self.$$(selector)) {
                    snapshot.add(tooltipText(tip));
                }
                throw new AssertionError("Страна \"" + countryName + "\" не исчезла. В DOM: " + snapshot);
            }

            com.codeborne.selenide.Selenide.sleep(100);
        }
    }

    public void absentTooltip() {
        waitMapReady();

        final String selector = "svg g[pointer-events='none'] text tspan";
         self.$$(selector).shouldBe(CollectionCondition.size(0));

    }

    private String tooltipText(SelenideElement element) {
        if (element == null || !element.exists()) return "";
        String raw = element.getAttribute("textContent");
        if (raw == null) return "";

        String normalized = raw.replace("\u00A0", " ")
                .trim()
                .replaceAll("\\s+", " ");

        return normalized;
    }

    public void waitMapReady() {
        self.shouldBe(visible, Duration.ofSeconds(8));
        self.$$("path").shouldHave(size(175));
    }
}
