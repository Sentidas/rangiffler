package ru.sentidas.rangiffler.test.web.feed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.FeedPage;

import static ru.sentidas.rangiffler.utils.AnnotationHelper.countryName;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.myPhoto;
// проверка значка страны при создании
// проверка значка страны при изменении
// проверка фото страны при создании
// проверка фото страны при изменении
// проверка загрузки других документов картинок
// проверка загрузки других документов не картинок
// проверка порядка при изменении (не первый)
// поискв фото по страницам назад
// урлы в фото
// урлы в странах

@WebTest
public class PhotoTest {

    private static final String TEST_IMAGE_PATH = "photo/4.png";
    private static final String TEST_IMAGE_BIG_SIZE = "disable/big_size.gif";
    private static final String CANADA = CountryName.labelByCode("ca");
    private static final String SPAIN = CountryName.labelByCode("es");
    private static final String ITALIA = CountryName.labelByCode("it");
    private static final String TEST_DESCRIPTION = "я наконец тут";

    @Test
    @User
    @ApiLogin
    @DisplayName("Создание поста: появляется алерт об успехе и новая карточка видна в ленте")
    void createPostShowsSuccessAndCardPresent() {
        FeedPage feed = new FeedPage()
                .addPhoto()
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(CANADA)
                .setPhotoDescription(TEST_DESCRIPTION)
                .save();

        feed.checkAlert("New post created")
                .checkExistPost(CANADA, TEST_DESCRIPTION);
    }

    @CsvSource({
            "photo/1.png, png",
            "photo/27.jpg, jpg",
            "photo/28.gif, gif"
    })
    @ParameterizedTest(name = "[{index}] {1} -> {0}")
    @User
    @ApiLogin
    @DisplayName("Создание поста: допустимые форматы изображений (jpg/png/gif/webp)")
    void createPostWithPermittedImageFormats(String image, String ext) {
        FeedPage feed = new FeedPage()
                .addPhoto()
                .uploadNewImage(image)
                .setNewCountry(CANADA)
                .setPhotoDescription(TEST_DESCRIPTION)
                .save();

        feed.checkAlert("New post created")
                .checkExistPost(CANADA, TEST_DESCRIPTION);

    }

    @CsvSource({
            "disable/1.src, src",
            "disable/2.docx, docx"
    })
    @ParameterizedTest(name = "[{index}] {1} -> {0}")
    @User
    @ApiLogin
    @DisplayName("Создание поста: недопустимые форматы изображений отклоняются")
    void createPostWithUnsupportedImageFormatsShowsError(String image, String ext, AppUser user) {

        FeedPage feed = new FeedPage()
                .addPhoto()
                .uploadNewImage(image)
                .setNewCountry(SPAIN)
                .setPhotoDescription(TEST_DESCRIPTION)
                .save();

        feed.checkAlert("Can not create new post. " +
                "Unsupported image format. Allowed: image/jpeg, image/png, image/gif, image/webp");
    }

    @CsvSource({
            "photo/1.png, png",
            "photo/27.jpg, jpg",
            "photo/28.gif, gif"
    })
    @ParameterizedTest(name = "[{index}] {1} -> {0}")
    @User(photo = 2)
    @ApiLogin
    @DisplayName("Обновление поста: допустимые форматы изображений (jpg/png/gif/webp)")
    void updatePostWithPermittedImageFormats(String image, String ext, AppUser user) {
        final AppPhoto photo = myPhoto(user, 1);
        final String countryBefore = countryName(photo.countryCode());
        final String descriptionBefore = photo.description();

        FeedPage feed = new FeedPage()
                .editPhoto(countryBefore, descriptionBefore)
                .uploadNewImage(image)
                .save();

        feed.checkAlert("Post updated");
    }

    @CsvSource({
            "disable/1.src, src",
            "disable/2.docx, docx"

    })
    @ParameterizedTest(name = "[{index}] {1} -> {0}")
    @User(photo = 2)
    @ApiLogin
    @DisplayName("Обновление поста: недопустимые форматы изображений отклоняются")
    void updatePostWithUnsupportedImageFormatsShowsError(String image, String ext, AppUser user) {
        final AppPhoto target = myPhoto(user, 1);
        final String countryBefore = countryName(target.countryCode());
        final String descriptionBefore = target.description();

        FeedPage feed = new FeedPage()
                .editPhoto(countryBefore, descriptionBefore)
                .uploadNewImage(image)
                .save();

        feed.checkAlert("Can not update post. " +
                "Unsupported image format. Allowed: image/jpeg, image/png, image/gif, image/webp");
    }

    @Test
    @User(photo = 2)
    @ApiLogin
    @DisplayName("Создание поста при наличии других постов: карточка появляется первой и виден алерт об успехе")
    void createPostAfterExistingPostsShowsAsFirst() {
        FeedPage feed = new FeedPage()
                .addPhoto()
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(CANADA)
                .setPhotoDescription(TEST_DESCRIPTION)
                .save();

        feed.checkAlert("New post created")
                .checkExistPost(CANADA, TEST_DESCRIPTION)
                .checkThatPostFirst(CANADA, TEST_DESCRIPTION);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Создание поста без изображения: отображение алерта и запрет сохранения")
    void createPostWithoutImageShowsValidationError() {
        new FeedPage()
                .addPhoto()
                .setNewCountry(CANADA)
                .setPhotoDescription(TEST_DESCRIPTION)
                .savePhotoUnsuccessful()
                .checkErrorMessage("Please upload an image");
    }

    // fix
    @Test
    @User
    @ApiLogin
    @DisplayName("Создание поста c изображением более 19МБ: отображение алерта и запрет сохранения")
    void createPostWithBigSizeImageShowsValidationError() {
        new FeedPage()
                .addPhoto()
                .uploadNewImage(TEST_IMAGE_BIG_SIZE)
                .setNewCountry(CANADA)
                .savePhotoUnsuccessful()
                .checkErrorMessage("Can not create new post. Limit 19 MB");
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Создание поста без описания: пост создаётся (описание пустое)")
    void createPostWithoutDescriptionCreatesWithEmptyDescription() {
        FeedPage feed = new FeedPage()
                .addPhoto()
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(CANADA)
                .save();

        feed.checkAlert("New post created")
                .checkExistPost(CANADA, null);
    }

    @Test
    @User(photo = 1)
    @ApiLogin
    @DisplayName("Редактирование поста: смена страны и описания приводит к обновлению карточки")
    void editPost(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 0);
        final String description = myPhoto.description();
        final String countryName = countryName(myPhoto.countryCode());

        FeedPage feed = new FeedPage()
                .editPhoto(countryName, description)
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(SPAIN)
                .setPhotoDescription("rename: " + description)
                .save();

        feed.checkAlert("Post updated")
                .checkExistPost(SPAIN, "rename: " + description);
    }


    @Test
    @User(photos = {@Photo(countryCode = "ru")})
    @ApiLogin
    @DisplayName("Редактирование поста: смена страны и изображения приводит к обновлению карточки")
    void editPostCountryAndImageUpdated(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 0);
        final String description = myPhoto.description();
        final String countryName = countryName(myPhoto.countryCode());

        FeedPage feed = new FeedPage()
                .editPhoto(countryName, description)
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(ITALIA)
                .setPhotoDescription("rename: " + description)
                .save();

        feed.checkAlert("Post updated")
                .checkExistPost(ITALIA, "rename: " + description);
    }


    @Test
    @ApiLogin
    @User(photo = 1)
    @DisplayName("Отмена редактирования: изменения не сохраняются, карточка остаётся прежней")
    void cancelEditPostKeepsOriginalCard(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 0);
        final String description = myPhoto.description();
        final String countryName = countryName(myPhoto.countryCode());

        new FeedPage()
                .editPhoto(countryName, description)
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(SPAIN)
                .setPhotoDescription("test")
                .cancel();

        new FeedPage().checkExistPost(countryName, description);
    }

    @Test
    @User(photo = 1)
    @ApiLogin
    @DisplayName("Редактирование: выбор той же страны допустим, изменения описания сохраняются")
    void editPostWithSameCountryNameUpdatesDescription(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 0);
        final String description = myPhoto.description();
        final String countryName = countryName(myPhoto.countryCode());

        FeedPage feed = new FeedPage()
                .editPhoto(countryName, description)
                .uploadNewImage(TEST_IMAGE_PATH)
                .setNewCountry(countryName)
                .setPhotoDescription("repeatCountryName: " + description)
                .save();

        feed.checkAlert("Post updated")
                .checkExistPost(countryName, "repeatCountryName: " + description);
    }

    @Test
    @User(photo = 4)
    @ApiLogin
    @DisplayName("Удаление поста: карточка удаляется и показывается алерт об удалении")
    void deletePostRemovesCard(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 2);
        final String description = myPhoto.description();
        final String countryName = countryName(myPhoto.countryCode());


        FeedPage feed = new FeedPage()
                .deletePhoto(countryName, description);

        feed.checkAlert("Post deleted")
                .checkNotExistPost(countryName, description);
    }
}
