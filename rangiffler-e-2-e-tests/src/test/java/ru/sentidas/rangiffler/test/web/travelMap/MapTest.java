package ru.sentidas.rangiffler.test.web.travelMap;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.ScreenShotTest;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.service.impl.PhotoApiClient;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@WebTest
@DisplayName("Web_Карта стран")
public class MapTest {

    private final PhotoApiClient apiClient = new PhotoApiClient();

    private static final String CANADA = CountryName.labelByCode("ca");
    private static final String CHINA = CountryName.labelByCode("cn");
    private static final String CUBA = CountryName.labelByCode("cu");
    private static final String TEST_DESCRIPTION = "я наконец тут";
    private static final String TEST_IMAGE_PATH = "photo/5.png";


    @Test
    @User
    @ApiLogin
    @ScreenShotTest(value = "screenshots/expected_map_empty.png")
    @DisplayName("Заливка: пустая карта без фото (обе вкладки)")
    void mapFillEmptyOnBothTabsWhenNoPhotos(BufferedImage expectedMap) {
        assertMapOnBothTabs(new FeedPage(), expectedMap, expectedMap);
    }

    @Test
    @ApiLogin
    @User(photos = {
            @Photo(countryCode = "ca", count = 2),
            @Photo(countryCode = "ru"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "in", count = 2),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "fr")
    }, friends = 2)
    @ScreenShotTest(files = {
            "screenshots/no_action_no_change/my.png",
            "screenshots/no_action_no_change/friends.png"
    })
    @DisplayName("Заливка: без действий не меняется (обе вкладки)")
    void mapFillUnchangedOnBothTabsWithoutActions(BufferedImage my, BufferedImage friends) {
        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, my, friends);
        assertMapOnBothTabs(feedPage, my, friends);
    }

    @Test
    @User(friends = 2,
            photos = {
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "in", count = 2),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cu", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "in", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "fr", count = 3)
            })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/expected_map_empty.png",
            "screenshots/my_empty_friends_fill/friends.png",
    })
    @DisplayName("Заливка: пусто на 'Мои фото', показана на 'Фото с друзьями'")
    void mapFillEmptyOnMyAndPresentOnFriends(BufferedImage my, BufferedImage friends) {
        assertMapOnBothTabs(new FeedPage(), my, friends);
    }

    @Test
    @User(friends = 1,
            photos = {
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "in")

            })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_empty_delete_friends/before.png",
            "screenshots/expected_map_empty.png"
    })
    @DisplayName("С друзьями — заливка: исчезает после удаления единственного фото друга")
    void friendsCountryFillDisappearsWhenDeletingOnlyFriendPhoto(AppUser user, BufferedImage before, BufferedImage after) {
        UUID friendId = friendId(user, 0);
        UUID photoId = firstPhotoId(user, friendId);
        FeedPage feedPage = new FeedPage();

        assertMapOnTabWithMyFriends(feedPage, before);
        apiClient.deletePhoto(photoId, friendId);
        assertMapOnTabWithMyFriends(feedPage, after);

    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там")
    })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_clear/before.png",
            "screenshots/fill_clear/after.png"
    })
    @DisplayName("Заливка: исчезает после удаления моего фото")
    void countryFillDisappearsAfterDeletingMyPhoto(BufferedImage before, BufferedImage after, AppUser user) {
        final AppPhoto appPhoto = firstPhoto(user, user.id());

        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, before, before);

        feedPage.deletePhoto(
                countryName(appPhoto.countryCode()),
                appPhoto.description());

        assertMapOnBothTabs(feedPage, after, after);
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "cn", description = "я тут")
    })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_no_change/before.png",
            "screenshots/fill_no_change/after.png"
    })
    @DisplayName("Заливка: не меняется при изменении описания")
    void countryFillUnchangedWhenUpdatingDescription(BufferedImage before, BufferedImage after) throws IOException {
        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, before, before);

        feedPage.editPhoto(
                        CHINA,
                        "я тут")
                .setPhotoDescription("остаюсь тут жить")
                .save();

        assertMapOnBothTabs(feedPage, after, after);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "cn")
    })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_no_change/before.png",
            "screenshots/fill_no_change/after.png"
    })
    @DisplayName("Заливка: не меняется при замене изображения")
    void countryFillUnchangedWhenReplacingImage(AppUser user, BufferedImage before, BufferedImage after) {
        final String description = myPhoto(user, 0).description();

        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, before, before);

        feedPage.editPhoto(
                        CHINA,
                        description)
                .uploadNewImage(TEST_IMAGE_PATH)
                .save();

        assertMapOnBothTabs(feedPage, after, after);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "cn", description = "я тут")
    })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_change/before.png",
            "screenshots/fill_change/after.png"
    })
    @DisplayName("Заливка: меняется при смене страны у моего фото")
    void countryFillChangesWhenChangingMyPhotoCountry(BufferedImage before, BufferedImage after) throws IOException {
        FeedPage feedPage = Selenide.open(FeedPage.URL, FeedPage.class);
        assertMapOnBothTabs(feedPage, before, before);

        feedPage.editPhoto(
                        CHINA,
                        "я тут")
                .setNewCountry(CUBA)
                .save();

        assertMapOnBothTabs(feedPage, after, after);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там")
    }, friends = 2)
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_add_friend_one_each/before.png",
            "screenshots/fill_add_friend_one_each/after_my_only.png",
            "screenshots/fill_add_friend_one_each/after_with_friends.png"
    })
    @DisplayName("С друзьями — заливка: появляется после добавления фото друзьями")
    void friendsCountryFillAppearsAfterFriendsAddPhotos(AppUser user,
                                                        BufferedImage before,
                                                        BufferedImage afterOnlyMy,
                                                        BufferedImage afterWithFriends) {
        final UUID friendId1 = friendId(user, 0);
        final UUID friendId2 = friendId(user, 1);

        FeedPage feedPage = new FeedPage();

        assertMapOnBothTabs(feedPage, before, before);

        apiClient.createPhoto(friendId1, "us");
        apiClient.createPhoto(friendId2, "in");

        Selenide.refresh();
        assertMapOnBothTabs(feedPage, afterOnlyMy, afterWithFriends);
    }

    @Test
    @User(friends = 2,
            photos = {
                    @Photo(countryCode = "ca", description = "я тут"),
                    @Photo(countryCode = "ru", description = "я там"),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "us"),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "in")
            })
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_change_friend_delete/before.png",
            "screenshots/fill_change_friend_delete/after.png"
    })
    @DisplayName("С друзьями — заливка: исчезает после удаления фото друга в этой стране")
    void friendsCountryFillDisappearsAfterDeletingFriendPhotoInCountry(AppUser user,
                                                                       BufferedImage beforeWithFriends,
                                                                       BufferedImage afterWithFriends) {
        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);

        FeedPage feedPage = new FeedPage();
        assertMapOnTabWithMyFriends(feedPage, beforeWithFriends);

        apiClient.deletePhoto(photoId, friendId);

        Selenide.refresh();
        assertMapOnTabWithMyFriends(feedPage, afterWithFriends);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "us"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "in")
    }, friends = 2)
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_change_friend_update_code/before.png",
            "screenshots/fill_change_friend_update_code/after.png"
    })
    @DisplayName("Фото с друзьями — заливка: меняется страна при смене страны у фото друга")
    void friendsCountryFillMovesWhenFriendChangesCountry(AppUser user,
                                                         BufferedImage beforeWithFriends,
                                                         BufferedImage afterWithFriends) {

        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);

        FeedPage feedPage = new FeedPage();

        assertMapOnTabWithMyFriends(feedPage, beforeWithFriends);

        apiClient.updatePhoto(photoId, friendId, "jp");

        Selenide.refresh();
        assertMapOnTabWithMyFriends(feedPage, afterWithFriends);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "us"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "in")
    }, friends = 2)
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_change_friend_update_code/before.png",
            "screenshots/fill_change_friend_update_code/before.png"
    })
    @DisplayName("С друзьями — заливка: не меняется при изменении описания у друга")
    void friendsCountryFillUnchangedWhenFriendUpdatesDescription(AppUser user,
                                                                 BufferedImage beforeWithFriends,
                                                                 BufferedImage afterWithFriends) {

        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);

        FeedPage feedPage = new FeedPage();

        assertMapOnTabWithMyFriends(feedPage, beforeWithFriends);

        apiClient.updatePhoto(photoId, friendId, null, null, "good");

        Selenide.refresh();
        assertMapOnTabWithMyFriends(feedPage, afterWithFriends);

    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "us"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "in")
    }, friends = 2)
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/fill_change_friend_update_code/before.png",
            "screenshots/fill_change_friend_update_code/before.png"
    })
    @DisplayName("С друзьями — заливка: не меняется при замене изображения у друга")
    void friendsCountryFillUnchangedWhenFriendReplacesImage(AppUser user,
                                                            BufferedImage beforeWithFriends,
                                                            BufferedImage afterWithFriends) {

        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);

        FeedPage feedPage = new FeedPage();

        assertMapOnTabWithMyFriends(feedPage, beforeWithFriends);

        apiClient.updatePhoto(photoId, friendId, "photo/5.png", null, null);

        Selenide.refresh();
        assertMapOnTabWithMyFriends(feedPage, afterWithFriends);

    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", count = 10),
            @Photo(countryCode = "ru", count = 9),
            @Photo(countryCode = "us", count = 8),
            @Photo(countryCode = "fr", count = 7),
            @Photo(countryCode = "in", count = 1),

            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "in", count = 2),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "fr", count = 1),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cu", count = 1),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "in", count = 1),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "fr", count = 3)
    }, friends = 2)
    @ApiLogin
    @ScreenShotTest(files = {
            "screenshots/opacity_variable/my.png",
            "screenshots/opacity_variable/friends.png"
    })
    @DisplayName("Заливка: интенсивность различается на 'Фото с друзьями' при разном количестве фото")
    void mapFillOpacityVariesOnFriendsWithDifferentPhotoCounts(BufferedImage my,
                                                               BufferedImage friends) {
        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, my, friends);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", count = 10),
            @Photo(countryCode = "ru", count = 9),
            @Photo(countryCode = "us", count = 8),
            @Photo(countryCode = "fr", count = 7),
            @Photo(countryCode = "ch", count = 6),
            @Photo(countryCode = "in", count = 5),
            @Photo(countryCode = "cu", count = 4),
            @Photo(countryCode = "au", count = 3),
            @Photo(countryCode = "tr", count = 2),
            @Photo(countryCode = "br", count = 1)
    })
    @ApiLogin
    @ScreenShotTest(value = "screenshots/opacity_variable.png")
    @DisplayName("Заливка: интенсивность различается на 'Мои фото' при разном количестве фото")
    void mapFillOpacityVariesOnMyWithDifferentPhotoCounts(BufferedImage expectedMap) {
        FeedPage feedPage = new FeedPage();
        assertMapOnBothTabs(feedPage, expectedMap);
    }

    private static void assertMapOnBothTabs(FeedPage page, BufferedImage expected) {
        if (page.isMyActive()) {
            page.checkMap(expected)
                    .openFriendsFeed()
                    .checkMap(expected);
        } else {
            page.checkMap(expected)
                    .openMyFeed()
                    .checkMap(expected);
        }
    }

    private static void assertMapOnBothTabs(FeedPage page, BufferedImage my, BufferedImage friends) {
        if (page.isMyActive()) {
            page.checkMap(my)
                    .openFriendsFeed()
                    .checkMap(friends);
        } else {
            page.checkMap(friends)
                    .openMyFeed()
                    .checkMap(my);
        }
    }

    private static void assertMapOnMyTab(FeedPage page, BufferedImage image) {
        page.openMyFeed()
                .checkMap(image);
    }

    private static void assertMapOnTabWithMyFriends(FeedPage page, BufferedImage image) {
        page.openFriendsFeed()
                .checkMap(image);
    }
}
