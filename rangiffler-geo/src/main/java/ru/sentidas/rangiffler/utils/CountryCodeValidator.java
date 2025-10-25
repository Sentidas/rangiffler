package ru.sentidas.rangiffler.utils;

public final class CountryCodeValidator {
    private CountryCodeValidator() {}

    public static String normalizeAndValidate(String code) {
        if (code == null) throw new IllegalArgumentException("country_code must not be null");
        String c = code.trim().toLowerCase();
        if (!c.matches("^[a-z]{2}$")) {
            throw new IllegalArgumentException("country_code must be two lowercase letters (ISO alpha-2), e.g. 'fr'");
        }
        return c;
    }
}
