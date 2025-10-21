package ru.sentidas.rangiffler.test.gql.mutation.photo;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetFeedQuery;
import ru.sentidas.LikePhotoMutation;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FeedApi;
import ru.sentidas.rangiffler.test.gql.api.PhotoApi;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.ErrorGql.classification;
import static ru.sentidas.rangiffler.test.gql.support.PhotoUtil.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.firstFriendId;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.firstPhotoId;

@DisplayName("GQL_Лайки на фото")
public class LikePhotoTest extends BaseGraphQlTest {

    private final PhotoApi photoApi = new PhotoApi(apolloClient);
    private final FeedApi feedApi = new FeedApi(apolloClient);


    @Test
    @User(friends = 1, photos = {
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0)
    })
    @ApiLogin
    @DisplayName("Лайк фото друга (лайков не было): увеличивается счётчик и есть мой userId")
    void likePhotoShouldIncreaseTotalAndContainMyUserIdWhenLikingFriendsPhoto(@Token String bearerToken, AppUser user) {
        final UUID friendId = firstFriendId(user);
        final UUID friendPhotoId = firstPhotoId(user, friendId);

        // Подготовка: проверка отсутствия лайков на фото
        GetFeedQuery.Data beforeFeedRes = feedApi.getFeed(bearerToken, 0, 10, true);
        GetFeedQuery.Node beforePhoto = findPhotoById(beforeFeedRes, friendPhotoId.toString());

        final int totalBefore = likesTotal(beforePhoto);
        assertFalse(containsLikeFromUser(beforePhoto, user.id()), "precondition: my like must be absent");

        // Действие: лайкаем фото друга
        LikePhotoMutation.Data likedRes =
                photoApi.likePhoto(bearerToken, friendPhotoId.toString(), user.id().toString());

        assertAll("mutation response",
                () -> assertNotNull(likedRes.photo),
                () -> assertEquals(friendPhotoId.toString(), likedRes.photo.id)
        );

        // Пост-проверка: мой лайк появился и счётчик увеличился на 1
        GetFeedQuery.Data afterFeedRes = feedApi.getFeed(bearerToken, 0, 10, true);
        GetFeedQuery.Node afterPhoto = findPhotoById(afterFeedRes, friendPhotoId.toString());

        assertAll("after like",
                () -> assertTrue(containsLikeFromUser(afterPhoto, user.id()), "my like must be present"),
                () -> assertEquals(totalBefore + 1, likesTotal(afterPhoto), "likes.total must be exactly +1")
        );
    }

    @Test
    @User(friends = 1, photos = {
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0)
    })
    @ApiLogin
    @DisplayName("Повторный мой лайк: снимает лайк (toggle), счётчик -1")
    void likePhotoTogglesOffOnSecondLike(@Token String bearerToken, AppUser user) {
        final UUID friendId = firstFriendId(user);
        final UUID friendPhotoId = firstPhotoId(user, friendId);

        // Действие (шаг 1): ставим лайк, включение (toggle on)
        photoApi.likePhoto(bearerToken, friendPhotoId.toString(), user.id().toString());

        GetFeedQuery.Data feedAfterFirstLike = feedApi.getFeed(bearerToken, 0, 10, true);
        GetFeedQuery.Node friendPhotoAfterFirstLike = findPhotoById(feedAfterFirstLike, friendPhotoId.toString());
        assertTrue(containsLikeFromUser(friendPhotoAfterFirstLike, user.id()), "precondition: like must be on");

        final int totalBeforeOff = likesTotal(friendPhotoAfterFirstLike);

        // Действие (шаг 2): повторный лайк — выключение (toggle off)
        photoApi.likePhoto(bearerToken, friendPhotoId.toString(), user.id().toString());

        GetFeedQuery.Data feedAfterSecondLike = feedApi.getFeed(bearerToken, 0, 10, true);
        GetFeedQuery.Node friendPhotoAfterSecondLike = findPhotoById(feedAfterSecondLike, friendPhotoId.toString());
        assertAll("after toggle off",
                () -> assertFalse(containsLikeFromUser(friendPhotoAfterSecondLike, user.id()), "my like must be removed"),
                () -> assertEquals(totalBeforeOff - 1, likesTotal(friendPhotoAfterSecondLike), "likes.total must be exactly -1")
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", count = 1)})
    @ApiLogin
    @DisplayName("Лайк своего фото: FORBIDDEN, счётчик не меняется")
    void likeOwnPhotoReturnsForbiddenAndDoesNotChangeCounter(@Token String bearerToken, AppUser user) {

        // Подготовка: моё фото и счётчик до попытки
        final UUID myPhotoId = AnnotationHelper.firstMyPhotoId(user);
        GetFeedQuery.Data beforeFeed = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = findPhotoById(beforeFeed, myPhotoId.toString());
        final int totalBefore = likesTotal(beforeNode);

        // Действие: попытка лайкнуть своё фото
        ApolloResponse<LikePhotoMutation.Data> resp =
                photoApi.tryLikePhotoRaw(bearerToken, myPhotoId.toString(), user.id().toString());

        // Проверка: ожидаем FORBIDDEN и корректный путь ошибки
        assertAll("forbidden self-like",
                () -> assertTrue(resp.hasErrors(), "response must contain errors"),
                () -> assertEquals("FORBIDDEN", classification(resp), "classification must be FORBIDDEN"),
                () -> assertEquals("Self-like is not allowed", resp.errors.getFirst().getMessage(), "message must be 'Self-like is not allowed'"),
                () -> assertTrue(resp.errors.get(0).getPath().contains("photo"), "error path must reference 'photo'")
        );

        // Пост-проверка: счётчик лайков не изменился
        GetFeedQuery.Data afterFeed = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = findPhotoById(afterFeed, myPhotoId.toString());
        assertEquals(totalBefore, likesTotal(afterNode), "likes.total must remain unchanged");
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Лайк несуществующего photo_id: ошибка NOT_FOUND")
    void likePhotoMustReturnNotFoundWhenIdDoesNotExist(@Token String bearerToken, AppUser user) {
        final String randomId = UUID.randomUUID().toString();

        ApolloResponse<LikePhotoMutation.Data> resp =
                photoApi.tryLikePhotoRaw(bearerToken, randomId, user.id().toString());

        assertAll("not found on like",
                () -> assertTrue(resp.hasErrors(), "response must contain errors"),
                () -> {
                    Map<String, Object> extensions = resp.errors.get(0).getExtensions();
                    assertEquals("NOT_FOUND", extensions.get("classification"), "classification must be NOT_FOUND");
                },
                () -> assertTrue(resp.errors.get(0).getPath().contains("photo"), "error path must contain 'photo'")
        );
    }
}
