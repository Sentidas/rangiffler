package ru.sentidas.rangiffler.test.web.user;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.ScreenShotTest;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.ProfilePage;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.UserData;

import java.awt.image.BufferedImage;

import static ru.sentidas.rangiffler.utils.AnnotationHelper.countryName;

@WebTest
@DisplayName("Web_Профиль пользователя")
public class ProfileTest {

    private static final String RUSSIAN_FEDERATION = CountryName.labelByCode("ru");
    private static final String CHINA = CountryName.labelByCode("cn");
    private static final String TEST_IMAGE_PATH = "avatar/3.png";

    @Test
    @User
    @ApiLogin
    @DisplayName("Имя пользователя отображается в профиле и не является кликабельным")
    void checkUsernameShouldBeVisibleAndNotClickableWhenProfileIsOpened(AppUser user) {
        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .checkThatPageLoaded()
                .checkUsername(user.username());
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Пустой профиль заполняется корректно: имя, фамилия, страна, аватар и алерт об успехе")
    @ScreenShotTest(value = "screenshots/expected-avatar.png")
    void fillEmptyProfileShouldPersistAllFieldsAndShowSuccessAlertWhenUserSaves(AppUser user, BufferedImage expectedAvatar) {
        final String username = user.username();
        final UserData userData = GenerationDataUser.randomUser();
        final String firstname = userData.firstname();
        final String surname = userData.surname();
        final String countryName = countryName(userData.countryCode());

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .checkLocation(RUSSIAN_FEDERATION)
                .uploadPhotoFromClasspath(TEST_IMAGE_PATH)
                .setFirstname(firstname)
                .setSurname(surname)
                .setNewCountry(countryName)
                .save();

        profile
                .checkAlert("Your profile is successfully updated")
                .checkUsername(username)
                .checkFirstname(firstname)
                .checkSurname(surname)
                .checkLocation(countryName)
                .checkAvatar(expectedAvatar);
    }

    @Test
    @User(full = true)
    @ApiLogin
    @DisplayName("При корректном обновлении имени/фамилии/страны отображается алерт и данные сохраняются")
    void updateProfileShouldShowSuccessAlertWhenUserSavesValidData(AppUser user) {
        final String username = user.username();
        final UserData userData = GenerationDataUser.randomUser();
        final String firstname = userData.firstname();
        final String surname = userData.surname();
        final String countryName = countryName(userData.countryCode());

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .setFirstname(firstname)
                .setSurname(surname)
                .setNewCountry(countryName)
                .save();


        profile
                .checkThatPageLoaded()
                .checkAlert("Your profile is successfully updated")
                .checkUsername(username)
                .checkFirstname(firstname)
                .checkSurname(surname)
                .checkLocation(countryName);
    }

    @Test
    @User(full = true)
    @ApiLogin
    @DisplayName("Сброс изменений имени/фамилии/страны возвращает исходные имя, фамилию и страну")
    void resetProfileShouldRestoreInitialValuesWhenUserCancelsChanges(AppUser user) {
        final String firstname = user.firstname();
        final String surname = user.surname();
        final String countryName = countryName(user.countryCode());

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .setFirstname("new firstname")
                .setSurname("new surname")
                .setNewCountry(CHINA)
                .reset();

        profile
                .checkFirstname(firstname)
                .checkSurname(surname)
                .checkLocation(countryName);
    }

    @Test
    @User(full = true)
    @ApiLogin
    @ScreenShotTest(value = "screenshots/expected-avatar.png")
    @DisplayName("При обновлении аватара сохраняется новый аватар и отображается алерт об успехе")
    void updateAvatarShouldPersistNewImageAndShowSuccessAlertWhenUserSaves(BufferedImage expectedAvatar, AppUser user) {
        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .uploadPhotoFromClasspath(TEST_IMAGE_PATH)
                .save();

        profile.checkAvatarExist()
                .checkAvatar(expectedAvatar)
                .checkAlert("Your profile is successfully updated");
    }
}
