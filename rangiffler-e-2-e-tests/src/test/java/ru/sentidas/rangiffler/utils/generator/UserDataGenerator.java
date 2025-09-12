package ru.sentidas.rangiffler.utils.generator;

import com.github.javafaker.Faker;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class UserDataGenerator {

    private UserDataGenerator() {
    }

    private static final Random random = new Random();

    private static final List<String> COUNTRY_POOL = List.of(
            "RU","US","ES","CN","IN","AE","BR","FR","BD","DE"
    );

    private static final List<String> FAKER_LOCALES = List.of(
            "ru","en","es","zh-CN","ar","pt-BR","pt-PT","fr","de","hi","bn"
    );


    public static UserData randomUser() {
        String countryCode = pickCountryCode();
        String locale = languageTagByCountry(countryCode); // "ru", "en-US", "pt-BR" и т.п.
        Faker faker = new Faker(safeFakerLocale(locale));


        return new UserData(
                countryCode,
                faker.name().firstName(),
                faker.name().lastName()
        );

    }

    private static String pickCountryCode() {
        return COUNTRY_POOL.get(random.nextInt(COUNTRY_POOL.size()));
    }

    // Маппинг страна -> язык/языковой тег.
    // Возвращай короткий язык ("ru") или язык-страну ("pt-BR") — оба корректно обработает forLanguageTag.
    private static String languageTagByCountry(String cc) {
        if (cc == null) return "en";
        switch (cc.trim().toUpperCase(Locale.ROOT)) {
            case "RU":
                return "ru";        // Россия
            case "US":
            case "GB":
            case "AU":
            case "NZ":
            case "CA":
            case "IE":
                return "en";        // англоязычные страны
            case "ES":
            case "MX":
            case "AR":
            case "CO":
                return "es";        // испанский
            case "CN":
                return "zh-CN";     // Китай (упрощённый китайский)
            case "IN":
                return "hi";        // хинди
            case "SA":
            case "AE":
            case "EG":
            case "MA":
                return "ar";        // арабский
            case "BR":
                return "pt-BR";     // Бразилия — чаще pt-BR, а не pt-PT
            case "PT":
                return "pt-PT";     // Португалия
            case "FR":
                return "fr";
            case "BD":
                return "bn";        // бенгальский
            case "DE":
                return "de";
            default:
                return "en";        // fallback
        }
    }

    private static Locale safeFakerLocale(String tag) {
        if (!FAKER_LOCALES.contains(tag)) {
            tag = "en";
        }
        return Locale.forLanguageTag(tag);
    }
}
