package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.ScreenShotTest;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.ProfilePage;
import ru.sentidas.rangiffler.utils.generator.UserData;
import ru.sentidas.rangiffler.utils.generator.UserDataGenerator;

import java.awt.image.BufferedImage;
// тест на ошибку Can not update user
// баг в update, при записи поверх старых данных - проходит тест, как будто данные до этого были стерты
@WebTest
public class ProfileTest {

    @Test
    @User
    @ApiLogin
    void checkUsernameInProfileAndNotClickable(AppUser user) {
        String username = user.username();

        Selenide.open(ProfilePage.URL, ProfilePage.class)
                .checkThatPageLoaded()
                .checkUsername(username);
    }

    @Test
    @User(empty = true)
    @ApiLogin
    @ScreenShotTest(value = "screenshots/expected-avatar.png")
    void fillEmptyUserProfile(AppUser user, BufferedImage expectedAvatar) {
        final String username = user.username();
        final UserData userData = UserDataGenerator.randomUser();
        final String firstname = userData.firstname();
        final String surname = userData.surname();
        final String countryName = CountryName.labelByCode(userData.countryCode());


        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .uploadPhotoFromClasspath("avatar/4.png")
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
    @User
    @ApiLogin
    void updateUserProfile(AppUser user) {
        final String username = user.username();
        System.out.println(username);
        final UserData userData = UserDataGenerator.randomUser();
        final String firstname = userData.firstname();
        final String surname = userData.surname();
        final String countryName = CountryName.labelByCode(userData.countryCode());

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .setFirstname(firstname)
                .setSurname(surname)
                .setNewCountry(countryName)
                .save();


        Selenide.refresh();
        new ProfilePage()
                .checkThatPageLoaded()
                .checkAlert("Your profile is successfully updated")
                .checkUsername(username)
                .checkFirstname(firstname)
                .checkSurname(surname)
                .checkLocation(countryName);
    }

    @Test
    @User
    @ApiLogin
    void resetUserProfile(AppUser user) {
        String firstname = user.firstname();
        String surname = user.surname();
        String countryName = CountryName.labelByCode(user.countryCode());

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .setFirstname("new firstname")
                .setSurname("new surname")
                .setNewCountry("China")
                .reset();

        profile
                .checkFirstname(firstname)
                .checkSurname(surname)
                .checkLocation(countryName);
    }

    @Test
    @User
    @ApiLogin
    @ScreenShotTest(value = "screenshots/expected-avatar.png")
    void updateAvatarUserProfile(BufferedImage expectedAvatar) {

        ProfilePage profile = Selenide.open(ProfilePage.URL, ProfilePage.class)
                .uploadPhotoFromClasspath("avatar/4.png")
                .save();

        profile.checkAvatarExist()
                .checkAvatar(expectedAvatar)
                .checkAlert("Your profile is successfully updated");
    }
}
