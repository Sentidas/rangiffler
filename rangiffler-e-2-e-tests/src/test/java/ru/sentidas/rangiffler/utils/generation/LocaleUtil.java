package ru.sentidas.rangiffler.utils.generation;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Класс: LocaleUtil
 * Назначение: нормализация и маппинг локалей/языков к единому нижнему регистру,
 *             выдача корректного тега для Faker.
 */
public final class LocaleUtil {

    private LocaleUtil() {}

    // Поддерживаемые faker-локали (lower-case)
    private static final Set<String> SUPPORTED_FAKER = Set.of(
            "en", "ru", "es", "zh-cn", "ar", "pt-br", "pt-pt", "fr", "de", "hi", "bn"
    );

    // Страна -> язык/локаль (все lower-case)
    private static final Map<String, String> COUNTRY_TO_TAG = Map.ofEntries(
            Map.entry("ru", "ru"),
            Map.entry("us", "en"), Map.entry("gb", "en"), Map.entry("au", "en"),
            Map.entry("nz", "en"), Map.entry("ca", "en"), Map.entry("ie", "en"),
            Map.entry("es", "es"), Map.entry("mx", "es"), Map.entry("ar", "es"), Map.entry("co", "es"),
            Map.entry("cn", "zh-cn"),
            Map.entry("in", "hi"),
            Map.entry("sa", "ar"), Map.entry("ae", "ar"), Map.entry("eg", "ar"), Map.entry("ma", "ar"),
            Map.entry("br", "pt-br"),
            Map.entry("pt", "pt-pt"),
            Map.entry("fr", "fr"),
            Map.entry("bd", "bn"),
            Map.entry("de", "de")
    );

    /** "PT-BR" -> "pt-br", "EN" -> "en", null -> "en" */
    public static String normalize(String tag) {
        if (tag == null || tag.isBlank()) return "en";
        return tag.trim().toLowerCase(Locale.ROOT);
    }

    /** Возвращает нормализованный языковой тег по коду страны (две буквы). Неизвестно → "en". */
    public static String tagByCountry(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) return "en";
        String cc = countryCode.trim().toLowerCase(Locale.ROOT);
        return COUNTRY_TO_TAG.getOrDefault(cc, "en");
    }

    /** Тег для Faker: если не поддерживается — "en". */
    public static String fakerLocale(String tag) {
        String norm = normalize(tag);
        return SUPPORTED_FAKER.contains(norm) ? norm : "en";
    }

    /** Превращает тег в Locale. */
    public static Locale toLocale(String tag) {
        return Locale.forLanguageTag(normalize(tag));
    }
}
