package ru.sentidas.rangiffler.test.gql.query;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetFeedQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FeedApi;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.ErrorGql.*;
import static ru.sentidas.rangiffler.test.gql.support.FeedUtil.countyCountFromStat;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

public class FeedGqlTest extends BaseGraphQlTest {

    FeedApi feedApi = new FeedApi(apolloClient);

    @Test
    @User(friends = 2,
            photos = {
                    @Photo(countryCode = "fr", count = 3),
                    @Photo(countryCode = "cn", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cn", count = 2),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "fr", count = 2)
            })
    @ApiLogin
    @DisplayName("Пагинация ленты: корректные флаги hasPrevious/hasNext для моих фото и с друзьями")
    void feedReturnsCorrectPaginationFlagsWhenPagingMyPhotosAndWithFriends(@Token String bearerToken) {

        // Мои фото: страница 0 размером 3
        GetFeedQuery.Data myFeedPage0 = feedApi.getFeed(bearerToken, 0, 3, false);
        assertAll("my photos page 0",
                () -> assertEquals(3, myFeedPage0.feed.photos.edges.size(), "page size must be 3"),
                () -> assertEquals(false, myFeedPage0.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be false"),
                () -> assertEquals(true, myFeedPage0.feed.photos.pageInfo.hasNextPage, "hasNextPage must be true")
        );

        // Мои фото: страница 1 размером 3
        GetFeedQuery.Data myFeedPage1 = feedApi.getFeed(bearerToken, 1, 3, false);
        assertAll("my photos page 1",
                () -> assertEquals(1, myFeedPage1.feed.photos.edges.size(), "page size must be 1"),
                () -> assertEquals(true, myFeedPage1.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be true"),
                () -> assertEquals(false, myFeedPage1.feed.photos.pageInfo.hasNextPage, "hasNextPage must be false")
        );

        // С друзьями: страница 0 размером 4
        GetFeedQuery.Data withFriendsPage0 = feedApi.getFeed(bearerToken, 0, 4, true);
        assertAll("with friends page 0",
                () -> assertEquals(4, withFriendsPage0.feed.photos.edges.size(), "page size must be 4"),
                // CHANGED: раньше тут по ошибке проверялся pageInfo из другого ответа (page0)
                () -> assertEquals(false, withFriendsPage0.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be false"),
                () -> assertEquals(true, withFriendsPage0.feed.photos.pageInfo.hasNextPage, "hasNextPage must be true")
        );

        // С друзьями: страница 1 размером 4
        GetFeedQuery.Data withFriendsPage1 = feedApi.getFeed(bearerToken, 1, 4, true);
        assertAll("with friends page 1",
                () -> assertEquals(4, withFriendsPage1.feed.photos.edges.size(), "page size must be 4"),
                () -> assertEquals(true, withFriendsPage1.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be true"),
                () -> assertEquals(true, withFriendsPage1.feed.photos.pageInfo.hasNextPage, "hasNextPage must be true")
        );

        // С друзьями: страница 2 размером 4
        GetFeedQuery.Data withFriendsPage2 = feedApi.getFeed(bearerToken, 2, 4, true);
        assertAll("with friends page 2",
                () -> assertEquals(1, withFriendsPage2.feed.photos.edges.size(), "page size must be 1"),
                () -> assertEquals(true, withFriendsPage2.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be true"),
                () -> assertEquals(false, withFriendsPage2.feed.photos.pageInfo.hasNextPage, "hasNextPage must be false")
        );
    }

    @Test
    @DisplayName("Агрегация статистики по странам: отдельно мои и мои+друзья консистентны на любой странице")
    @User(friends = 2,
            photos = {
                    @Photo(countryCode = "fr", count = 3),
                    @Photo(countryCode = "cn", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "ru", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cn", count = 2),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "fr", count = 2)
            })
    @ApiLogin
    void feedAggregatesCountryStatConsistentlyAcrossPages(@Token String bearerToken, AppUser user) {
        final Map<String, Integer> expectedMine = expectedMyCounts(user);
        final Map<String, Integer> expectedWithFriends = expectedFeedCounts(user);

        GetFeedQuery.Data myPage0 = feedApi.getFeed(bearerToken, 0, 4, false);
        assertEquals(expectedMine, countyCountFromStat(myPage0), "my stat must be consistent on page 0");

        GetFeedQuery.Data myPage1 = feedApi.getFeed(bearerToken, 1, 4, false);
        assertEquals(expectedMine, countyCountFromStat(myPage1), "my stat must be consistent on page 1");

        GetFeedQuery.Data withFriends0 = feedApi.getFeed(bearerToken, 0, 6, true);
        assertEquals(expectedWithFriends, countyCountFromStat(withFriends0), "my+friends stat must be consistent on page 0");

        GetFeedQuery.Data withFriends1 = feedApi.getFeed(bearerToken, 1, 6, true);
        assertEquals(expectedWithFriends, countyCountFromStat(withFriends1), "my+friends stat must be consistent on page 1");
    }


    @Test
    @User(friends = 2,
            photos = {@Photo(countryCode = "it", count = 1, likes = 2)})
    @ApiLogin
    @DisplayName("Лайки на моих фото: total совпадает с размером массива и равен ожидаемому")
    void feedShowsExpectedLikesOnMyPhotos(@Token String bearerToken, AppUser user) {
        GetFeedQuery.Data feedPage = feedApi.getFeed(bearerToken, 0, 4, false);

        for (GetFeedQuery.Edge edge : feedPage.feed.photos.edges) {
            GetFeedQuery.Node node = edge.node;
            assertAll("likes",
                    () -> assertNotNull(node.likes, "likes must be non-null"),
                    () -> assertEquals(node.likes.total, node.likes.likes.size(), "likes.total must equal likes array size"),
                    () -> assertEquals(2, node.likes.total, "likes.total must equal expected value")
            );
        }
    }

    @Test
    @User(friends = 2, photos = {
            @Photo(countryCode = "fr", count = 2, likes = 1),
            @Photo(countryCode = "it", count = 1, likes = 2),
    })
    @ApiLogin
    @DisplayName("Мои фото: все лайки только от друзей, без самолайка")
    void myPhotosContainOnlyFriendsLikesAndNoSelfLike(@Token String bearerToken, AppUser user) {
        final List<UUID> friendIds = friendIds(user);

        GetFeedQuery.Data feedPage = feedApi.getFeed(bearerToken, 0, 6, false);

        for (GetFeedQuery.Edge edge : feedPage.feed.photos.edges) {
            GetFeedQuery.Node node = edge.node;
            for (GetFeedQuery.Like like : node.likes.likes) {
                UUID likerId = UUID.fromString(like.user);
                assertAll("like constraints",
                        () -> assertTrue(friendIds.contains(likerId), "liker must be a friend"),
                        () -> assertFalse(likerId.equals(user.id()), "liker must not be the owner")
                );
            }
        }
    }

    @Test
    @User(friends = 2,
            photos = {
                    @Photo(countryCode = "fr", count = 1, likes = 0), // мои фото — для контекста
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "de", count = 1, likes = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "es", count = 1, likes = 1)
            })
    @ApiLogin
    @DisplayName("Фото друзей: на каждом фото друга есть один лайк от меня")
    void friendsPhotosAlwaysContainSingleLikeFromMeWhenWithFriends(@Token String bearerToken, AppUser user) {
        final List<UUID> myPhotoIds = myPhotoIds(user);
        GetFeedQuery.Data feedPage = feedApi.getFeed(bearerToken, 0, 10, true);

        for (GetFeedQuery.Edge edge : feedPage.feed.photos.edges) {
            GetFeedQuery.Node node = edge.node;
            UUID photoId = UUID.fromString(node.id);

            // проверяем только фото друзей
            if (!myPhotoIds.contains(photoId)) {
                assertAll("friend photo likes",
                        () -> assertEquals(1, node.likes.total, "friends' photo must have exactly one like"),
                        () -> assertEquals(1, node.likes.likes.size(), "likes array must contain exactly one item"),
                        () -> assertEquals(user.id().toString(), node.likes.likes.get(0).user, "the like must be from the current user")
                );
            }
        }
    }

    @Test
    @User(friends = 5,
            photos = {
                    @Photo(countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "it", count = 1)
            })
    @ApiLogin
    @DisplayName("Если лайков нет, приходит пустой блок likes с total=0 и пустым массивом")
    void feedShowsEmptyLikesBlockWhenNoLikes(@Token String bearerToken) {
        GetFeedQuery.Data myFeedPage = feedApi.getFeed(bearerToken, 0, 4, false);
        for (GetFeedQuery.Edge edge : myFeedPage.feed.photos.edges) {
            GetFeedQuery.Node node = edge.node;
            assertAll("empty likes on my photos",
                    () -> assertNotNull(node.likes, "likes must be non-null"),
                    () -> assertEquals(0, node.likes.total, "likes.total must be 0"),
                    () -> assertEquals(0, node.likes.likes.size(), "likes array must be empty")
            );
        }

        GetFeedQuery.Data withFriendsFeedPage = feedApi.getFeed(bearerToken, 0, 4, true);
        for (GetFeedQuery.Edge edge : withFriendsFeedPage.feed.photos.edges) {
            GetFeedQuery.Node node = edge.node;
            assertAll("empty likes on withFriends feed",
                    () -> assertNotNull(node.likes, "likes must be non-null"),
                    () -> assertEquals(0, node.likes.total, "likes.total must be 0"),
                    () -> assertEquals(0, node.likes.likes.size(), "likes array must be empty")
            );
        }
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Если нет фото ни у меня ни у друзей — пустые edges/stat и корректные флаги пагинации")
    void feedReturnsEmptyEdgesAndStatWhenNoPhotosAnywhere(@Token String bearerToken) {
        GetFeedQuery.Data myFeedPage = feedApi.getFeed(bearerToken, 0, 3, false);
        assertAll("my empty feed",
                () -> assertEquals(0, myFeedPage.feed.photos.edges.size(), "edges must be empty"),
                () -> assertEquals(0, myFeedPage.feed.stat.size(), "stat must be empty"),
                () -> assertEquals(false, myFeedPage.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be false"),
                () -> assertEquals(false, myFeedPage.feed.photos.pageInfo.hasNextPage, "hasNextPage must be false")
        );

        GetFeedQuery.Data withFriendsFeedPage = feedApi.getFeed(bearerToken, 1, 3, true);
        assertAll("withFriends empty feed",
                () -> assertEquals(0, withFriendsFeedPage.feed.photos.edges.size(), "edges must be empty"),
                () -> assertEquals(0, withFriendsFeedPage.feed.stat.size(), "stat must be empty"),
                () -> assertEquals(false, withFriendsFeedPage.feed.photos.pageInfo.hasPreviousPage, "hasPreviousPage must be false"),
                () -> assertEquals(false, withFriendsFeedPage.feed.photos.pageInfo.hasNextPage, "hasNextPage must be false")
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", count = 2)})
    @ApiLogin
    @DisplayName("Если запрошена страница вне диапазона — пустые edges и корректный pageInfo")
    void feedReturnsEmptyEdgesAndValidPageInfoWhenPageOutOfRange(@Token String bearerToken) {
        GetFeedQuery.Data feedPage = feedApi.getFeed(bearerToken, 3, 10, false);
        assertAll("out-of-range page",
                () -> assertNotNull(feedPage.feed.photos.edges, "edges must be non-null"),
                () -> assertEquals(0, feedPage.feed.photos.edges.size(), "edges must be empty"),
                () -> assertNotNull(feedPage.feed.photos.pageInfo, "pageInfo must be non-null")
        );
    }

    @Test
    @User(friends = 1,
            photos = {
                    @Photo(countryCode = "fr", description = "я тут"),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, description = "", countryCode = "it")
            })
    @ApiLogin
    @DisplayName("Данные фото: id UUID, src корректного типа, description")
    void feedReturnsPhotosWithValidIdsSrcAndDescriptions(@Token String bearerToken) {
        GetFeedQuery.Data response = feedApi.getFeed(bearerToken, 3, 10, false);

        int index = 0;
        for (GetFeedQuery.Edge edge : response.feed.photos.edges) {
            GetFeedQuery.Node photo = edge.node;
            int i = index++;

            String id = photo.id;
            String src = photo.src;
            String description = photo.description;

            boolean isHttpUrl = src != null && (src.startsWith("http://") || src.startsWith("https://"));
            boolean isDataUrl = src != null && src.startsWith("data:image/");

            assertAll("photo node #" + i,
                    () -> assertNotNull(id, "id must be non-null"),
                    () -> assertFalse(id.isEmpty(), "id must be non-empty"),
                    () -> assertDoesNotThrow(() -> UUID.fromString(id), "id must be a valid UUID"),
                    () -> assertNotNull(src, "src must be non-null"),
                    () -> assertFalse(src.isEmpty(), "src must be non-empty"),
                    () -> assertTrue(isHttpUrl || isDataUrl, "src must be http(s) URL or data:image/... dataURL"),
                    () -> assertNotNull(description, "description must be a non-null string")
            );
        }
    }

    @Test
    @User(friends = 1,
            photos = {
                    @Photo(countryCode = "fr", description = "я тут"),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, description = "", countryCode = "it")
            })
    @ApiLogin
    @DisplayName("withFriends=true: id фото в выдаче принадлежат либо мне, либо друзьям")
    void feedWithFriendsReturnsIdsBelongingToMineOrFriends(@Token String bearerToken, AppUser user) {
        final List<String> myPhotoIds = myPhotoIdsToString(user);
        final List<String> friendsPhotoIds = friendsPhotoIds(user);

        GetFeedQuery.Data feedPage = feedApi.getFeed(bearerToken, 0, 10, true);

        int index = 0;
        for (GetFeedQuery.Edge edge : feedPage.feed.photos.edges) {
            int i = index++;
            String photoId = edge.node.id;
            assertAll("photo node #" + i,
                    () -> assertNotNull(photoId, "id must be non-null"),
                    () -> assertFalse(photoId.isEmpty(), "id must be non-empty"),
                    () -> assertTrue(myPhotoIds.contains(photoId) || friendsPhotoIds.contains(photoId),
                            "id must belong to generated my/friends photos")
            );
        }
    }

    @Test
    @User
    @DisplayName("Авторизация: запрос без токена должен отклоняться предсказуемой ошибкой")
    void getFeedMustReturnAuthErrorWhenTokenIsMissing() {

        ApolloResponse<GetFeedQuery.Data> unauthorizedResponse =
                feedApi.tryGetFeedWithoutAuth(0, 2, true);

        assertAll("FORBIDDEN on update without authorization",
                () -> assertTrue(unauthorizedResponse.hasErrors(), "response must contain errors"),
                () -> assertNull(unauthorizedResponse.data, "data must be null on auth failure"),
                () -> assertEquals("FORBIDDEN", classification(unauthorizedResponse), "classification must be FORBIDDEN"),
                () -> assertEquals("feed", path(unauthorizedResponse), "error path must be 'user'"),
                () -> assertEquals("Access is denied", message(unauthorizedResponse), "message must be 'Access is denied'")
        );
    }
}
