package ru.sentidas.rangiffler.utils.generation;

import com.github.javafaker.Faker;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.utils.ImageHelper;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Класс: GenerationDataUser
 * - имя/фамилия/юзернейм/тексты
 * - страна
 * - картинки (classpath: avatar/*, photo/*)
 * - доменные хелперы: randomUser(), randomPhoto()
 * Описания фото — в отдельном классе PhotoDescriptions.
 */
public final class GenerationDataUser {

    private GenerationDataUser() {
    }

    private static Faker fakerForTag(String tag) {
        String normalized = LocaleUtil.fakerLocale(tag);
        return new Faker(LocaleUtil.toLocale(normalized));
    }

    // --- Username & text ---

    public static String randomUsername() {
        Faker f = fakerForTag("en");
        int suffix = ThreadLocalRandom.current().nextInt(10_000);
        String base = f.animal().name().replaceAll("\\s+", "_");
        if (base.length() < 3) base = "user";
        return base + suffix;
    }

    @Nonnull
    public static String getUniqueTestUsername() {
        return "testUser_" + System.currentTimeMillis() / 1000;
    }

    public static String randomSurname(String localeTag) {
        return fakerForTag(localeTag).name().lastName();
    }

    public static String randomSentence(int wordsCount) {
        return String.join(" ", fakerForTag("en").lorem().words(Math.max(1, wordsCount)));
    }

    public static String randomSentenceByLength(int length) {
        return fakerForTag("en").lorem().characters(length);
    }

    // --- Countries ---

    public static String randomCountryCode() {
        return CountryName.randomCode();
    }

    // --- Images via classpath ---

    private static final String[] EXTS = {"png", "jpg", "gif", "webp"};

    private static boolean resourceExists(String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResource(resourcePath) != null;
    }

    public static String randomAvatarDataUrl() {
        int idx = ThreadLocalRandom.current().nextInt(1, 57 + 1);
        String rp = null;
        for (String ext : EXTS) {
            String cand = "avatar/" + idx + "." + ext;
            if (resourceExists(cand)) {
                rp = cand;
                break;
            }
        }
        if (rp == null) throw new IllegalStateException("avatar/" + idx + ".* не найден (png|jpg|gif|webp)");
        return ImageHelper.fromClasspath(rp).toDataUrl();
    }

    public static String randomPhotoDataUrl() {
        int idx = ThreadLocalRandom.current().nextInt(1, 34 + 1);
        String rp = null;
        for (String ext : EXTS) {
            String cand = "photo/" + idx + "." + ext;
            if (resourceExists(cand)) {
                rp = cand;
                break;
            }
        }
        if (rp == null) throw new IllegalStateException("photo/" + idx + ".* не найден (png|jpg|gif|webp)");
        return ImageHelper.fromClasspath(rp).toDataUrl();
    }

    // --- Domain helpers ---

    // Тот же список стран для «локации» пользователя, что и в исходнике.
    private static final List<String> COUNTRY_USER_LOCATION = List.of(
            "ru", "us", "gb", "au", "nz", "ca", "ie", "es", "mx", "ar", "co",
            "cn", "in", "sa", "ae", "eg", "ma", "pt", "br", "fr", "bd", "de"
    );

    private static String randomUserCountry() {
        int i = ThreadLocalRandom.current().nextInt(COUNTRY_USER_LOCATION.size());
        return COUNTRY_USER_LOCATION.get(i);
    }

    public static UserData randomUser() {
        String countryCode = randomUserCountry();
        String tag = LocaleUtil.tagByCountry(countryCode);
        Faker f = fakerForTag(tag);
        return new UserData(countryCode, f.name().firstName(), f.name().lastName());
    }

    public static AppPhoto randomPhoto(java.util.UUID ownerId, String ownerCountryCode, String travelCountryCodeOrNull) {
        String ownerTag = LocaleUtil.tagByCountry(ownerCountryCode);
        String description = PhotoDescriptions.pickRandom(ownerTag);
        String travelCode = (travelCountryCodeOrNull != null && !travelCountryCodeOrNull.isBlank())
                ? travelCountryCodeOrNull
                : randomCountryCode();
        return new AppPhoto(
                null,
                ownerId,
                randomPhotoDataUrl(),
                travelCode,
                description,
                new Date(),
                0
        );
    }
}
