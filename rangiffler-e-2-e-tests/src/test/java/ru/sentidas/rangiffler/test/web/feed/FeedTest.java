package ru.sentidas.rangiffler.test.web.feed;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.service.impl.PhotoApiClient;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@WebTest
@DisplayName("Web_Лента фото")
public class FeedTest {
    private final PhotoApiClient clientPhoto = new PhotoApiClient();
    private static final String CANADA = CountryName.labelByCode("ca");
    private static final String SPAIN = CountryName.labelByCode("es");

    @Test
    @User(photo = 1, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Нельзя редактировать пост друга — алерт, фото без изменений")
    void editFriendPhotoForbidden(AppUser user) {
        final AppPhoto friendPhoto = firstPhoto(user, friendId(user, 0));
        final String description = friendPhoto.description();
        final String countryName = countryName(friendPhoto.countryCode());

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .editPhoto(countryName, description)
                .setNewCountry(SPAIN)
                .save();

        feed.checkAlert("Can not update post")
                .checkExistPost(countryName, description)
                .checkNotExistPost(SPAIN, description);
    }

    @Test
    @User(photo = 1, friends = 1, friendsWithPhotosEach = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: нельзя удалить пост друга — алерт, пост остаётся")
    void deleteFriendPhotoForbidden(AppUser user) {
        final UUID friendId = friendId(user, 0);
        final AppPhoto friendPhoto = firstPhoto(user, friendId);
        final String description = friendPhoto.description();
        final String countryName = countryName(friendPhoto.countryCode());

        final int countPhotosFriendBefore = clientPhoto.getCountPhotos(friendId);
        final int feedPhotosFriendBefore = clientPhoto.getFeedPhotos(friendId);

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .deletePhoto(countryName, description);

        feed.checkAlert("Can not delete post")
                .checkExistPost(countryName, description);

        final int countPhotosFriendAfter = clientPhoto.getCountPhotos(friendId);
        final int feedPhotosFriendAfter = clientPhoto.getFeedPhotos(friendId);

        assertEquals(countPhotosFriendBefore, countPhotosFriendAfter);
        assertEquals(feedPhotosFriendBefore, feedPhotosFriendAfter);
    }

    @Test
    @User(friends = 1, friendsWithPhotosEach = 2, photo = 2)
    @ApiLogin
    @DisplayName("Фото с друзьями: создание моего фото - фото появляется и становится первым")
    void createMyOnFriendsBecomesFirst() {
        final String desc = GenerationDataUser.randomSentence(5);

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .addPhoto("photo/4.png", CANADA, desc);

        feed.checkThatPostFirst(CANADA, desc);

        feed.openMyFeed()
                .checkThatPostFirst(CANADA, desc);
    }

    @Test
    @User(friends = 1, friendsWithPhotosEach = 2, photo = 2)
    @ApiLogin
    @DisplayName("Мои фото: редактирование фото не делает его первым в списке фото")
    void editPostDoesNotChangeOrder() {
        final String desc = GenerationDataUser.randomSentence(8);

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .addPhoto("photo/4.png", CANADA, desc);

        feed.checkThatPostFirst(CANADA, desc);

        feed.openMyFeed()
                .checkThatPostFirst(CANADA, desc);
    }

    @Test
    @User(friends = 1, friendsWithPhotosEach = 2, photo = 2)
    @ApiLogin
    @DisplayName("Создание фото делает его первым на 'Мои фото' и 'Фото с друзьями'")
    void createOnMyFeedFirstOnBothTabs() {
        final String desc = GenerationDataUser.randomSentence(5);

        FeedPage feed = new FeedPage()
                .addPhoto("photo/4.png", CANADA, desc);

        feed.checkThatPostFirst(CANADA, desc);

        feed.openFriendsFeed()
                .checkThatPostFirst(CANADA, desc);
    }

    @Test
    @User(friends = 2, friendsWithPhotosEach = 6, photo = 2)
    @ApiLogin
    @DisplayName("Две страницы: создание фото делает его первым на 'Мои фото' и 'Фото с друзьями'")
    void createOnFriendsTwoPageFeedFirstOnBoth() {
        final String desc = GenerationDataUser.randomSentence(7);

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .addPhoto("photo/4.png", CANADA, desc);

        feed.checkAlert("New post created")
                .checkThatPostFirst(CANADA, desc);

        feed.openMyFeed()
                .checkThatPostFirst(CANADA, desc);
    }

    @Test
    @User(photo = 3, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Мои фото: при добавлении фото увеличивается количество всех фото +1")
    void countersIncreaseAfterCreateOnMy(AppUser user) {
        final String desc = GenerationDataUser.randomSentence(5);
        final int countPhotosUserBefore = clientPhoto.getCountPhotos(user.id());
        final int feedPhotosBefore = clientPhoto.getFeedPhotos(user.id());

        Selenide.open(FeedPage.URL, FeedPage.class)
                .addPhoto("photo/4.png", CANADA, desc);

        final int countPhotosUserAfter = clientPhoto.getCountPhotos(user.id());
        final int feedPhotosAfter = clientPhoto.getFeedPhotos(user.id());

        assertEquals(countPhotosUserBefore + 1, countPhotosUserAfter);
        assertEquals(feedPhotosBefore + 1, feedPhotosAfter);
    }

    @Test
    @User(photo = 2, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Навигация: фото друга видно на 'Фото с друзьями' и отсутствует на 'Мои фото'")
    void friendPostNotVisibleOnMy(AppUser user) {
        final AppPhoto friendPhoto = firstPhoto(user, friendId(user, 0));
        final String description = friendPhoto.description();
        final String countryName = countryName(friendPhoto.countryCode());

        FeedPage feedPage = new FeedPage()
                .openFriendsFeed()
                .checkExistPost(countryName, description);

        feedPage.openMyFeed()
                .checkNotExistPost(countryName, description);
    }
}
