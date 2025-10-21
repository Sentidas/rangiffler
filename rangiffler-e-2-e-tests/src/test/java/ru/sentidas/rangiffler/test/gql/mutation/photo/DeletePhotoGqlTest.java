package ru.sentidas.rangiffler.test.gql.mutation.photo;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.DeletePhotoMutation;
import ru.sentidas.GetFeedQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FeedApi;
import ru.sentidas.rangiffler.test.gql.api.PhotoApi;
import ru.sentidas.rangiffler.test.gql.support.ErrorGql;

import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

public class DeletePhotoGqlTest extends BaseGraphQlTest {

    private final PhotoApi photoApi = new PhotoApi(apolloClient);
    private final FeedApi feedApi = new FeedApi(apolloClient);

    @Test
    @User(photos = {@Photo(countryCode = "es", description = "for delete")})
    @ApiLogin
    @DisplayName("Удаление фото: возвращает true и удаляет фото из ленты")
    void deletePhotoShouldReturnTrueAndRemovePhotoFromFeedWhenIdExists(@Token String bearerToken, AppUser user) {
        final String photoIdFromAnnotation = firstPhotoId(user, user.id()).toString();

        // лента до удаления фото
        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        final String photoIdFromFeedRes = before.feed.photos.edges.get(0).node.id;
        assertEquals(photoIdFromAnnotation, photoIdFromFeedRes);

        // удаление фото
        DeletePhotoMutation.Data data = photoApi.deletePhoto(bearerToken, photoIdFromFeedRes);
        assertTrue(data.deletePhoto.booleanValue(), "deletePhoto must return true for existing id");

        // проверка отсутствия удаленного фото в ленте
        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        final boolean present = after.feed.photos.edges.stream().anyMatch(e -> Objects.equals(e.node.id, photoIdFromFeedRes));
        assertFalse(present, "deleted photo must not be present in feed");
    }

    @Test
    @User(friends = 1,
            photos = {@Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "it", description = "друга")})
    @ApiLogin
    @DisplayName("Удаление чужого фото: отказ по доступу, фото остаётся")
    void deletePhotoMustFailWhenDeletingForeignPhoto(@Token String bearerToken, AppUser user) {
        final UUID friendId = firstFriendId(user);
        final UUID friendPhotoId = firstPhotoId(user, friendId);

        // лента до удаления фото
        GetFeedQuery.Data beforeFeedRes = feedApi.getFeed(bearerToken, 0, 10, true);


        // попытка удалить чужое фото
        ApolloResponse<DeletePhotoMutation.Data> tryDeleteRes
                = photoApi.tryDeletePhoto(bearerToken, friendPhotoId);

        assertAll("forbidden delete",
                () -> assertTrue(tryDeleteRes.hasErrors(), "response must contain errors"),
                () -> assertEquals(ErrorGql.message(tryDeleteRes), "Can`t access to photo"),
                () -> assertEquals("deletePhoto", ErrorGql.path(tryDeleteRes), "error path must contain 'deletePhoto'")
        );

        // проверка наличия удаляемого фото в ленте
        GetFeedQuery.Data afterFeedRes = feedApi.getFeed(bearerToken, 0, 10, true);
        final boolean present = afterFeedRes.feed.photos.edges.stream().anyMatch(e -> Objects.equals(e.node.id, friendPhotoId.toString()));
        assertTrue(present, "friend's photo must still be in feed");
        assertEquals(beforeFeedRes, afterFeedRes);
    }

    @Test
    @User(photos = {@Photo(countryCode = "es", description = "моё")})
    @ApiLogin
    @DisplayName("Удаление без авторизации: ошибка Unauthorized, фото остаётся")
    void deletePhotoMustReturnAuthErrorWhenTokenIsMissing(@Token String bearerToken, AppUser user) {
        final UUID photoId = firstMyPhotoId(user);
        GetFeedQuery.Data beforeFeedRes = feedApi.getFeed(bearerToken, 0, 4, false);

        // попытка удаления фото без авторизации
        final ApolloResponse<DeletePhotoMutation.Data> deleteWithoutAuthRes
                = photoApi.tryDeletePhotoWithoutAuth(photoId.toString());

        assertAll("unauthorized delete",
                () -> assertTrue(deleteWithoutAuthRes.hasErrors(), "response must contain errors")
        );

        // проверка наличия удаляемого фото в ленте
        GetFeedQuery.Data afterFeedRes = feedApi.getFeed(bearerToken, 0, 4, false);
        assertEquals(beforeFeedRes, afterFeedRes);
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Удаление несуществующего id: ошибка NOT_FOUND")
    void deletePhotoMustReturnNotFoundWhenIdDoesNotExist(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();

        // попытка удаления несуществующего фото
        ApolloResponse<DeletePhotoMutation.Data> tryDeleteRes = photoApi.tryDeletePhoto(bearerToken, randomId);

        assertAll("not found on delete",
                () -> assertTrue(tryDeleteRes.hasErrors(), "response must contain errors"),
                () -> assertEquals("NOT_FOUND", ErrorGql.classification(tryDeleteRes)),
                () -> assertEquals(ErrorGql.message(tryDeleteRes), "Can't find photo by id: " + randomId),
                () -> assertEquals("deletePhoto", ErrorGql.path(tryDeleteRes), "error path must contain 'deletePhoto'")
        );
    }
}
