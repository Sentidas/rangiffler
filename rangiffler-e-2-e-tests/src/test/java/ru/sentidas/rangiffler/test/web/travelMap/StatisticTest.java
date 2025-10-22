package ru.sentidas.rangiffler.test.web.travelMap;

import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.WebTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.page.FeedPage;
import ru.sentidas.rangiffler.service.PhotoApiClient;

import java.util.UUID;

import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@WebTest
@DisplayName("Web_Статистика по странам")
public class StatisticTest {

    private final PhotoApiClient apiClient = new PhotoApiClient();

    private static final String CANADA = CountryName.labelByCode("ca");
    private static final String SPAIN = CountryName.labelByCode("es");
    private static final String RUSSIA = "Russia";
    private static final String CUBA = CountryName.labelByCode("cu");
    private static final String SOUTH_AFRICA = CountryName.labelByCode("za");
    private static final String BRAZIL = CountryName.labelByCode("br");

    @Test
    @User
    @ApiLogin
    @DisplayName("0→0: Нет тултипов без фото (обе вкладки)")
    void noTooltipsWhenAbsentMyAndFriendsPhotos() {
        FeedPage feedPage = new FeedPage();
        assertNoTooltipsOnBothTabs(feedPage);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("0->1: Мои фото - тултип появляется после добавления первого фото")
    void countryTooltipAppearsWhenFirstPhotoAdded(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().absentTooltip(SPAIN);

        feedPage.addPhoto("photo/4.png", SPAIN, "я наконец тут");
        feedPage.map().expectedTooltip(SPAIN, 1);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("0→1:Фото с друзьями — тултип появляется после добавления моего первого фото")
    void countryTooltipAppearsWhenFirstPhotoAdded_FeedWithFriends(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().absentTooltip(SPAIN);

        feedPage.addPhoto("photo/4.png", SPAIN, "я наконец тут");

        feedPage.openFriendsFeed()
                .map().expectedTooltip(SPAIN, 1);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca")
    })
    @ApiLogin
    @DisplayName("1→2: Мои фото/Фото с друзьями — счётчик увеличивается после добавления моего второго фото (Canada)")
    void countryTooltipAppearsOnFriendsTabAfterAddingMyPhoto(AppUser user) {
        FeedPage page = new FeedPage();
        assertTooltipOnBothTabs(page, CANADA, 1);

        page.addPhoto("photo/4.png", CANADA, "я наконец тут");

        assertTooltipOnBothTabs(page, CANADA, 2);

    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca")
    })
    @ApiLogin
    @DisplayName("1→0: Мои фото — тултип исчезает после удаления единственного фото")
    void countryTooltipDisappearsAfterDeletingOnlyPhoto(AppUser user) {
        FeedPage page = new FeedPage();
        page.map().expectedTooltip(CANADA, 1);

        page.deletePhoto(
                CANADA,
                photoDescription(user, 0));

        page.map().absentTooltip(CANADA);
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru")
    })
    @ApiLogin
    @DisplayName("1->1: Мои фото: Тултип остаётся без изменений при удалении моего фото другой страны")
    void countryTooltipRemainsWhenOtherCountryDeleted(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().expectedTooltip(RUSSIA, 1);

        feedPage.deletePhoto(CANADA, "я тут");

        feedPage.map().expectedTooltip(RUSSIA, 1);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут"),
            @Photo(countryCode = "ru", description = "я там"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "ca")
    }, friends = 1)
    @ApiLogin
    @DisplayName("Удаление фото другом уменьшает счётчик во «с друзьями»")
    void countryTooltipDecrementsWhenFriendDeletePhoto(AppUser user) {
        FeedPage feedPage = new FeedPage();

        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);

        feedPage.openFriendsFeed()
                .map().expectedTooltip(CANADA, 2);

        apiClient.deletePhoto(photoId, friendId);
        Selenide.refresh();

        assertTooltipOnBothTabs(feedPage, CANADA, 1);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут")
    })
    @ApiLogin
    @DisplayName("1->1: Мои фото: Тултип переносится на новую страну после изменения страны у фото")
    void countryTooltipMovesToNewCountryAfterUpdate(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().expectedTooltip(CANADA, 1);

        feedPage.editPhoto(CANADA, "я тут")
                .setNewCountry(SPAIN)
                .save();

        feedPage.map().expectedTooltip(SPAIN, 1);
        feedPage.map().absentTooltip(CANADA);
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут")
    })
    @ApiLogin
    @DisplayName("Тултип не меняется при обновлении только описания")
    void countryTooltipUnchangedWhenOnlyDescriptionUpdated(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().expectedTooltip(CANADA, 1);

        feedPage.editPhoto(CANADA, "я тут")
                .setPhotoDescription("я там")
                .save();

        feedPage.map().expectedTooltip(CANADA, 1);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca", description = "я тут")
    })
    @ApiLogin
    @DisplayName("Тултип не меняется при обновлении только фото")
    void countryTooltipUnchangedWhenOnlyImageUpdated(AppUser user) {
        FeedPage feedPage = new FeedPage();
        feedPage.map().expectedTooltip(CANADA, 1);

        feedPage.editPhoto(CANADA, "я тут")
                .uploadNewImage("photo/1.png")
                .save();

        feedPage.map().expectedTooltip(CANADA, 1);
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "ca"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "ca")

    }, friends = 1)
    @ApiLogin
    @DisplayName("Фото с друзьями: счётчик агрегируется c фото друзей")
    void countryTooltipAggregatesOnFriendsTabAfterFriendAddsPhoto() {
        FeedPage feedPage = new FeedPage();
        assertTooltipOnBothTabs(feedPage, CANADA, 1, 2);
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "za", count = 3),
            @Photo(countryCode = "cu", count = 2),
            @Photo(countryCode = "ca")
    }, friends = 3)
    @ApiLogin
    @DisplayName("Фото с друзьями: счётчик агрегируется после добавления фото друзей")
    void countryTooltipAggregatesOnFriendsTabAfterFriendsAddsPhoto(AppUser user) {
        final UUID friendId1 = friendId(user, 0);
        final UUID friendId2 = friendId(user, 1);
        final UUID friendId3 = friendId(user, 2);
        FeedPage feedPage = new FeedPage();

        assertCountiesOnMy(feedPage,
                SOUTH_AFRICA, 3,
                CUBA, 2,
                CANADA, 1
        );

        assertCountriesOnFriends(feedPage,
                SOUTH_AFRICA, 3,
                CUBA, 2,
                CANADA, 1
        );

        apiClient.createPhoto(friendId1, "cu");
        apiClient.createPhoto(friendId2, "cu");
        apiClient.createPhoto(friendId3, "ru");
        Selenide.refresh();

        assertCountiesOnMy(feedPage,
                SOUTH_AFRICA, 3,
                CUBA, 2,
                CANADA, 1
        );

        assertCountriesOnFriends(feedPage,
                SOUTH_AFRICA, 3,
                CUBA, 4,
                CANADA, 1,
                RUSSIA, 1
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "ca"),
            @Photo(countryCode = "ru"),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "ca")
    }, friends = 1)
    @ApiLogin
    @DisplayName("Смена страны фото другом: −1 старой, +1 новой во «с друзьями»")
    void countryTooltipDecrementsWhenFriendChangeCountryCodeOnPhoto(AppUser user) {
        final UUID friendId = friendId(user, 0);
        final UUID photoId = firstPhotoId(user, friendId);
        FeedPage feedPage = new FeedPage();

        assertCountiesOnMy(feedPage,
                CANADA, 1,
                RUSSIA, 1
        );

        assertCountriesOnFriends(feedPage,
                CANADA, 2,
                RUSSIA, 1
        );

        apiClient.updatePhoto(photoId, friendId, "ru");
        Selenide.refresh();

        assertCountiesOnMy(feedPage,
                CANADA, 1,
                RUSSIA, 1
        );

        assertCountriesOnFriends(feedPage,
                CANADA, 1,
                RUSSIA, 2
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "br", count = 2),
            @Photo(countryCode = "cu"),
            @Photo(countryCode = "ca", count = 3),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cu", count = 2),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "br", count = 1),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "ru", count = 5)
    }, friends = 2)
    @ApiLogin
    @DisplayName("Фото с друзьями: счётчик не меняется после переключения вкладок")
    void shouldKeepCorrectCounts_WhenSwitchingBetweenMyFeedAndFriendsFeed() {
        FeedPage feedPage = new FeedPage();

        assertCountiesOnMy(feedPage,
                CUBA, 1,
                BRAZIL, 2,
                CANADA, 3
        );

        assertCountriesOnFriends(feedPage,
                CUBA, 3,
                BRAZIL, 3,
                CANADA, 3,
                RUSSIA, 5
        );

        assertCountiesOnMy(feedPage,
                CUBA, 1,
                BRAZIL, 2,
                CANADA, 3
        );

        assertCountriesOnFriends(feedPage,
                CUBA, 3,
                BRAZIL, 3,
                CANADA, 3,
                RUSSIA, 5
        );
    }

    private static void assertNoTooltipsOnBothTabs(FeedPage page) {
        if (page.isMyActive()) {
            page.map().absentTooltip();
            page.openFriendsFeed().map().absentTooltip();
        } else {
            page.map().absentTooltip();
            page.openMyFeed().map().absentTooltip();
        }
    }

    private static void assertTooltipOnBothTabs(FeedPage page, String country, int expectedCount) {
        if (page.isMyActive()) {
            page.map().expectedTooltip(country, expectedCount);
            page.openFriendsFeed().map().expectedTooltip(country, expectedCount);
        } else {
            page.map().expectedTooltip(country, expectedCount);
            page.openMyFeed().map().expectedTooltip(country, expectedCount);
        }
    }

    private static void assertTooltipOnBothTabs(FeedPage page, String country, int expectedMy, int expectedWithFriends) {
        if (page.isMyActive()) {
            page.map().expectedTooltip(country, expectedMy);
            page.openFriendsFeed().map().expectedTooltip(country, expectedWithFriends);
        } else {
            page.map().expectedTooltip(country, expectedWithFriends);
            page.openMyFeed().map().expectedTooltip(country, expectedMy);
        }
    }

    private static void assertCountiesOnMy(FeedPage p, Object... countryCountPairs) {
        ensureMy(p);
        for (int i = 0; i < countryCountPairs.length; i += 2) {
            String country = (String) countryCountPairs[i];
            int count = (int) countryCountPairs[i + 1];
            p.map().expectedTooltip(country, count);
        }
    }

    private static void assertCountriesOnFriends(FeedPage p, Object... countryCountPairs) {
        ensureFriends(p);
        for (int i = 0; i < countryCountPairs.length; i += 2) {
            String country = (String) countryCountPairs[i];
            int count = (int) countryCountPairs[i + 1];
            p.map().expectedTooltip(country, count);
        }
    }
    private static FeedPage ensureMy(FeedPage page) {
        return page.isMyActive() ? page : page.openMyFeed();
    }
    private static FeedPage ensureFriends(FeedPage page) {
        return page.isMyActive() ? page.openFriendsFeed() : page;
    }

}
