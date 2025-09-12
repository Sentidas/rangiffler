//package ru.sentidas.rangiffler.utils.generator;
//
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.ThreadLocalRandom;
//
//public class Generator2 {
//
//    private TravelCaptions() {}
//
//    // ---------- Публичные методы ----------
//
//    /** Случайная подпись по ISO-2 коду страны (например, "RU", "US"). */
//    public static String randomCaptionForCountry(String countryIso2) {
//        Locale loc = localeByCountry(countryIso2);
//        String lang = normalizeLang(loc);
//        List<String> list = CAPTIONS.getOrDefault(lang, EN);
//        return pick(list);
//    }
//
//    /** Случайная подпись по Locale. */
//    public static String randomCaptionForLocale(Locale locale) {
//        String lang = normalizeLang(locale);
//        List<String> list = CAPTIONS.getOrDefault(lang, EN);
//        return pick(list);
//    }
//
//    // ---------- Маппинг страна → Locale ----------
//    // добавлены страны, на которые «привязан» язык (комментарии справа)
//    private static Locale localeByCountry(String cc) {
//        if (cc == null) return Locale.ENGLISH;
//        switch (cc.trim().toUpperCase(Locale.ROOT)) {
//            case "RU": return Locale.forLanguageTag("ru-RU"); // Россия
//            case "US": // США
//            case "GB": // Великобритания
//            case "AU": // Австралия
//            case "NZ": // Новая Зеландия
//            case "CA": // Канада
//            case "IE": // Ирландия
//                return Locale.forLanguageTag("en-US");
//
//            case "CN": return Locale.forLanguageTag("zh-CN"); // Китай (упрощённый)
//            case "IN": return Locale.forLanguageTag("hi-IN"); // Индия (хинди)
//            case "ES": // Испания
//            case "MX": // Мексика
//            case "AR": // Аргентина
//            case "CO": // Колумбия
//                return Locale.forLanguageTag("es-ES");
//
//            case "SA": // Саудовская Аравия
//            case "AE": // ОАЭ
//            case "EG": // Египет
//            case "MA": // Марокко
//                return Locale.forLanguageTag("ar-SA");
//
//            case "BR": // Бразилия
//            case "PT": // Португалия
//                return Locale.forLanguageTag("pt-BR");
//
//            case "FR": return Locale.forLanguageTag("fr-FR"); // Франция
//            case "BD": return Locale.forLanguageTag("bn-BD"); // Бангладеш (бенгальский)
//            case "DE": return Locale.forLanguageTag("de-DE"); // Германия
//
//            default: return Locale.ENGLISH; // фолбэк
//        }
//    }
//
//    private static String normalizeLang(Locale locale) {
//        if (locale == null) return "en";
//        String lang = locale.getLanguage();
//        return switch (lang) {
//            case "ru" -> "ru";
//            case "en" -> "en";
//            case "es" -> "es";
//            case "zh" -> "zh";
//            case "hi" -> "hi";
//            case "ar" -> "ar";
//            case "pt" -> "pt";
//            case "fr" -> "fr";
//            case "bn" -> "bn";
//            case "de" -> "de";
//            default -> "en";
//        };
//    }
//
//    private static <T> T pick(List<T> list) {
//        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
//    }
//
//}
