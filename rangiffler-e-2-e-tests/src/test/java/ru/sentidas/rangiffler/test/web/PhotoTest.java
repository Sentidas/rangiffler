package ru.sentidas.rangiffler.test.web;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.model.CreatePhoto;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.page.ProfilePage;

// проверка значка страны при создании
// проверка значка страны при изменении
// проверка фото страны при создании
// проверка фото страны при изменении
// проверка загрузки других документов картинок
// проверка загрузки других документов не картинок
// проверка порядка при создании (первый)
// проверка порядка при изменении (не первый)
// проверка количества на странице
// поиск пофо по страницам вперед
// поискв фото по страницам назад
// урлы в фото
// урлы в странах
// починить стирание описания
// разделить тесты на FEED FEED_FRIEND EDIT CREATE
// починить невлезающие страны
// найти ограничение фронта на описание

@WebTest
public class PhotoTest {

    @Test
    @User
    @ApiLogin
    void createPost(AppUser user) {
        System.out.println(user.username());

        FeedPage feedPage = Selenide.open(FeedPage.URL, FeedPage.class)
                .addPhoto()
                .uploadNewImage("photo/4.png")
                .setNewCountry("Dominican Republic")
                .setPhotoDescription("я наконец тут")
                .savePhoto();

        feedPage
               // Post updated
                .checkExistPost("Dominican Republic8", "я nen  наконец тут")
                .checkAlert("New 5post created3") ; //Can not create new post
    }

    @Test
    @User
    @ApiLogin
    void createWithoutPhoto(AppUser user) {
        System.out.println(user.username());

        new FeedPage()
                .addPhoto()
                .setNewCountry("Dominican Republic")
                .setPhotoDescription("я наконец тут")
                .savePhoto();

      //  new CreatePag().checkErrorMessage("Please upload an image");
    }

    @Test
    @User
    @ApiLogin
    void createWithoutDescription(AppUser user) {
        System.out.println(user.username());

        new FeedPage()
                .addPhoto()
                .uploadNewImage("photo/6.png")
                .setNewCountry("Dominican Republic")
                .savePhoto();

        new FeedPage()
                .checkAlert("New post created")
                .checkExistPost("Dominican Republic", null); // поправить проверку null
    }

    @Test
    @User(photo = 1)
    @ApiLogin
    void editPost(AppUser user) {
        System.out.println(user.username());
        String countryNameBefore = CountryName.labelByCode(
                user.testData().photos().get(0).countryCode());
        String description = user.testData().photos().get(0).description();
        System.out.println("cтрана фото до: " + countryNameBefore);
        System.out.println("описание фото до: " + description);


        FeedPage feed = new FeedPage()
                .editPhoto(countryNameBefore, description)
                .uploadNewImage("photo/4.png")
                .setNewCountry("China")
                .setPhotoDescription("rename: " + description)
                .save();


        feed.checkAlert("Post updated")
                .checkExistPost("China", "rename: " + description);
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "ru")
    })
    @ApiLogin
    void editPost2(AppUser user) {
        System.out.println(user.username());
        String countryNameBefore = CountryName.labelByCode(
                user.testData().photos().get(0).countryCode());
        String description = user.testData().photos().get(0).description();
        System.out.println("cтрана фото до: " + countryNameBefore);
        System.out.println("описание фото до: " + description);


        new FeedPage()
                .editPhoto(countryNameBefore, description)
                .uploadNewImage("photo/4.png")
                .setNewCountry("Latvia") // выбор страны выше, проверить - не работает
                .setPhotoDescription("rename: " + description)
                .save();

        new FeedPage()
                .checkAlert("Post updated")
                .checkExistPost("Latvia", "rename: " + description);
    }


    @Test
    @ApiLogin
    @User(photo = 1)
    void cancelEditPost(AppUser user) {
        System.out.println(user.username());
        String countryNameBefore = CountryName.labelByCode(
                user.testData().photos().get(0).countryCode());
        String description = user.testData().photos().get(0).description();
        System.out.println("cтрана фото до: " + countryNameBefore);
        System.out.println("описание фото до: " + description);

        new FeedPage()
                .editPhoto(countryNameBefore, description)
                .uploadNewImage("photo/4.png")
                .setNewCountry("China")
                .setPhotoDescription("test")
                .cancel();

        new FeedPage()
                .checkExistPost(countryNameBefore, description);
    }

    @Test
    @User(photo = 1)
    @ApiLogin
    void repeatCountryName(AppUser user) {
        System.out.println(user.username());
        String countryNameBefore = CountryName.labelByCode(
                user.testData().photos().get(0).countryCode());
        String description = user.testData().photos().get(0).description();
        System.out.println("cтрана фото до: " + countryNameBefore);
        System.out.println("описание фото до: " + description);


        new FeedPage()
                .editPhoto(countryNameBefore, description)
                .uploadNewImage("photo/4.png")
                .setNewCountry(countryNameBefore)
                .setPhotoDescription("repeatCountryName: " + description)
                .save();

        new FeedPage()
                .checkAlert("Post updated") //Post deleted
                .checkExistPost(countryNameBefore, "repeatCountryName: " + description);
    }

    @Test
    @User(photo = 5)
    @ApiLogin
    void deletePost(AppUser user) {
        System.out.println(user.username());
        String countryNameBefore = CountryName.labelByCode(
                user.testData().photos().get(4).countryCode());
        String description = user.testData().photos().get(4).description();
        System.out.println("cтрана фото до: " + countryNameBefore);
        System.out.println("описание фото до: " + description);

        new FeedPage()
                .deletePhoto(countryNameBefore, description);

        new FeedPage()
                .checkAlert("Post deleted")
                .checkNotExistPost(countryNameBefore, description); // поправить
    }
}
