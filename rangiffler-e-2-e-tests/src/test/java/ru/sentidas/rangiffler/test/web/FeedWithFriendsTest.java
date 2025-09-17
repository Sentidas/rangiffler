package ru.sentidas.rangiffler.test.web;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.page.FeedPage;

@WebTest
public class FeedWithFriendsTest {

    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void editFriendPhoto(AppUser user) {
        System.out.println(user.username());
        AppUser friend = user.testData().friends().get(0);
        Photo photoFriend = friend.testData().photos().get(0);
        String description = photoFriend.description();
        String friendCountryName = CountryName.labelByCode(photoFriend.countryCode());
        System.out.println(friend.countryCode());
        System.out.println(friendCountryName);
        System.out.println(description);

        new FeedPage()
                .clickFeedWithFriend()
                .editPhoto(friendCountryName, description)
                .setNewCountry("China")
                .save();

        new FeedPage()
                .checkAlert("Can not update post"); //Can not delete post
    }

    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void deleteFriendPhoto(AppUser user) {
        System.out.println(user.username());
        AppUser friend = user.testData().friends().get(0);
        System.out.println(friend.username());
        Photo photoFriend = friend.testData().photos().get(0);
        String description = photoFriend.description();
        String friendCountryName = CountryName.labelByCode(photoFriend.countryCode());
        System.out.println(friend.countryCode());
        System.out.println(friendCountryName);
        System.out.println(description);

        new FeedPage()
                .clickFeedWithFriend()
                .deletePhoto(friendCountryName, description);

        new FeedPage()
                .checkAlert("Can not delete post");
    }


    @Test
    @User(photo = 4, friends = 1, friendsWithPhotosEach = 3)
    @ApiLogin
    void backToMyTravels(AppUser user) {
        System.out.println(user.username());
        AppUser friend = user.testData().friends().get(0);
        System.out.println(friend.username());
        Photo photoFriend = friend.testData().photos().get(0);
        String description = photoFriend.description();
        String friendCountryName = CountryName.labelByCode(photoFriend.countryCode());
        System.out.println(friend.countryCode());
        System.out.println(friendCountryName);
        System.out.println(description);

        new FeedPage()
                .clickFeedWithFriend()
                .checkExistPost(friendCountryName, description);

        new FeedPage()
                .clickMyTravels()
                .checkNotExistPost(friendCountryName, description);

    }
}
