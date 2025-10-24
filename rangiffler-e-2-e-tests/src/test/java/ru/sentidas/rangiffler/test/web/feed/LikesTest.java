package ru.sentidas.rangiffler.test.web.feed;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.service.impl.PhotoApiClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@WebTest
@DisplayName("Web_Лайки на фото")
public class LikesTest {

    private final PhotoApiClient photoApiClient = new PhotoApiClient();

    @Test
    @User(photo = 2, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Мои фото: при клике лайка на своём фото показывается алерт и лайк не проставляется")
    void likeOwnPostShowsAlertWhenOnMyFeed(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 1);
        final String countryName = countryName(myPhoto.countryCode());
        final String description = myPhoto.description();

        new FeedPage()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was not liked");
    }

    @Test
    @User(photo = 2, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Фото с друзьями: при клике лайка на своём фото показывается алерт и лайк не проставляется")
    void likeOwnPostShowsAlertOnFriendsFeed(AppUser user) {
        final AppPhoto myPhoto = myPhoto(user, 1);
        final String countryName = countryName(myPhoto.countryCode());
        final String description = myPhoto.description();

        new FeedPage()
                .openFriendsFeed()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was not liked");
    }

    @Test
    @User(photo = 2, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Фото с друзьями: лайк поста друга — счётчик увеличивается на 1, иконка становится заполненной")
    void likeFriendPostUpdatesIconAndCounterOnFriendsFeed(AppUser user) {
        final AppPhoto friendPhoto = friendPhoto(user, 0, 1);
        final String countryName = countryName(friendPhoto.countryCode());
        final String description = friendPhoto.description();

        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description);

        feed.assertCardLiked(countryName, description)
                .checkSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was successfully liked");
    }

    @Test
    @User(photo = 2, friends = 1, friendsWithPhotosEach = 2)
    @ApiLogin
    @DisplayName("Фото с друзьями: повторный клик — анлайк успешен, счётчик уменьшается на 1, иконка становится пустой")
    void secondClickOnFriendPostUnlikesAndUpdatesUIOnFriendsFeed(AppUser user) {
        final AppPhoto friendPhoto = friendPhoto(user, 0, 1);
        final String countryName = countryName(friendPhoto.countryCode());
        final String description = friendPhoto.description();

        // первый лайк фото друга - лайк есть
        FeedPage feed = new FeedPage()
                .openFriendsFeed()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description)
                .checkSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was successfully liked")
                .assertCardLiked(countryName, description);

        // повторный лайк того же фото друга - лайк снимается
        feed.likePhoto(countryName, description)
                .assertCardNotLiked(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was successfully unliked");
    }

    @Test
    @User(photos = {@Photo(likes = 2)}, friends = 2)
    @ApiLogin
    @DisplayName("Мои фото 2 -> 1: уменьшение счётчика лайков после анлайка другом (через backend)")
    void likesCounterDecreasesAfterUnlike(AppUser user) {
        final AppPhoto myPhoto = firstPhoto(user, user.id());
        final String countryName = countryName(myPhoto.countryCode());
        final String description = myPhoto.description();
        final UUID photoId = myPhoto.id();
        final UUID friendId = firstFriendId(user);

        FeedPage feed = new FeedPage();
        final int beforeCountLikes = feed.checkCountLikes(countryName, description);

        // повторный лайк (анлайк) другом, который поставил лайк изначально
        photoApiClient.likePhoto(photoId, friendId);
        Selenide.refresh();

        final int afterCountLikes = feed.checkCountLikes(countryName, description);
        assertEquals(beforeCountLikes - 1, afterCountLikes);

    }

    @Test
    @User(photos = {@Photo(likes = 1)}, friends = 1)
    @ApiLogin
    @DisplayName("Мои фото 1 -> 0: счётчик лайков уменьшается до нуля после анлайка другом (через backend)")
    void likesReachZeroAfterUnlike(AppUser user) {
        final AppPhoto myPhoto = firstPhoto(user, user.id());
        final String countryName = countryName(myPhoto.countryCode());
        final String description = myPhoto.description();
        final UUID photoId = myPhoto.id();
        final UUID friendId = firstFriendId(user);

        FeedPage feed = new FeedPage();
        final int likesBefore = new FeedPage().checkCountLikes(countryName, description);

        // повторный лайк (анлайк) другом, который поставил лайк изначально
        photoApiClient.likePhoto(photoId, friendId);

        Selenide.refresh();
        final int realCountLikesAfter = feed.checkCountLikes(countryName, description);

        assertEquals(likesBefore - 1, realCountLikesAfter);
    }

    @Test
    @User(photo = 2, friends = 1)
    @ApiLogin
    @DisplayName("Мои фото 0 -> 1: увеличение счётчика лайков после лайка другом (через backend)")
    void likesCounterIncreasesAfterLikeMyPhoto(AppUser user) {
        final AppPhoto myPhoto = firstPhoto(user, user.id());
        final String countryName = countryName(myPhoto.countryCode());
        final String description = myPhoto.description();
        final UUID photoId = myPhoto.id();
        final UUID friendId = firstFriendId(user);

        FeedPage feed = new FeedPage();
        final int beforeCountLikes = feed.checkCountLikes(countryName, description);

        photoApiClient.likePhoto(photoId, friendId);
        Selenide.refresh();

        final int afterCountLikes = feed.checkCountLikes(countryName, description);
        assertEquals(beforeCountLikes + 1, afterCountLikes);
    }
}
