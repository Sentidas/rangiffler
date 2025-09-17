package ru.sentidas.rangiffler.test.web;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.page.FeedPage;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WebTest
public class LikesTest {


    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void likePhotoYourself(AppUser user) {
        System.out.println(user.username());
        Photo photoUser = user.testData().photos().get(2);
        String countryName = CountryName.labelByCode(photoUser.countryCode());
        System.out.println(countryName);
        String description = photoUser.description();
        System.out.println(description);

        new FeedPage()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description);

        new FeedPage()
                .assertCardNotLiked(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was not liked");
    }

    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void likePhotoYourselfOnFriendsPage(AppUser user) {
        System.out.println(user.username());
        Photo photoUser = user.testData().photos().get(2);
        String countryName = CountryName.labelByCode(photoUser.countryCode());
        System.out.println(countryName);
        String description = photoUser.description();
        System.out.println(description);

        new FeedPage()
                .clickFeedWithFriend()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description);

        new FeedPage()
                .assertCardNotLiked(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was not liked");
    }

    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void likePhotoFriend(AppUser user) {
        System.out.println(user.username());
        AppUser friend = user.testData().friends().get(0);
        Photo photoFriend = friend.testData().photos().get(2);
        String countryName = CountryName.labelByCode(photoFriend.countryCode());
        String description = photoFriend.description();

        new FeedPage()
                .clickFeedWithFriend()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description);

        new FeedPage()
                .assertCardLiked(countryName, description)
                .checkSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was succesfully liked");
    }

    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void doubleClickOnLikePhotoFriend(AppUser user) {
        System.out.println(user.username());
        AppUser friend = user.testData().friends().get(0);
        Photo photoFriend = friend.testData().photos().get(2);
        String countryName = CountryName.labelByCode(photoFriend.countryCode());
        String description = photoFriend.description();

        new FeedPage()
                .clickFeedWithFriend()
                .assertCardNotLiked(countryName, description)
                .likePhoto(countryName, description);

        new FeedPage()
                .assertCardLiked(countryName, description)
                .checkSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was succesfully liked");

        new FeedPage()
                .assertCardLiked(countryName, description)
                .likePhoto(countryName, description);

        new FeedPage()
                .assertCardNotLiked(countryName, description)
                .checkUnSuccessLikesPhoto(countryName, description)
                .checkAlert("Post was succesfully liked");
    }

    @Test
    @User(photos = {
            @ru.sentidas.rangiffler.jupiter.annotaion.Photo(likes = 4)
    },
            friends = 4)
    @ApiLogin
        // залогиниться другом и снять лайк
    void likePhotoYourself3(AppUser user) {
        System.out.println(user.username());
        Photo photoUser = user.testData().photos().get(0);
        int realLikes = photoUser.likesTotal();
        System.out.println("realLikes:" + realLikes);
        String textRealLikes = realLikes + " likes";
        String countryName = CountryName.labelByCode(photoUser.countryCode());
        System.out.println(countryName);
        String description = photoUser.description();
        System.out.println(description);

        String all = new FeedPage()
                .checkAllLikes(countryName, description);

        assertEquals(all, textRealLikes);
    }
}
