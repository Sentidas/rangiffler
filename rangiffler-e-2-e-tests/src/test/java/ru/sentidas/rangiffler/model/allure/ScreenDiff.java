package ru.sentidas.rangiffler.model.allure;

public record ScreenDiff(
        String expected,
        String actual,
        String diff
) {
}
