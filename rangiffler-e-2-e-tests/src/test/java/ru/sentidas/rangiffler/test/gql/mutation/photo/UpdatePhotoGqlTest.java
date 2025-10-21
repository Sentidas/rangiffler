package ru.sentidas.rangiffler.test.gql.mutation.photo;

import com.apollographql.apollo.api.ApolloResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sentidas.GetFeedQuery;
import ru.sentidas.UpdatePhotoMutation;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FeedApi;
import ru.sentidas.rangiffler.test.gql.api.PhotoApi;
import ru.sentidas.rangiffler.utils.AnnotationHelper;
import ru.sentidas.rangiffler.test.gql.support.ErrorGql;
import ru.sentidas.rangiffler.utils.ImageDataUrl;
import ru.sentidas.rangiffler.test.gql.support.PhotoUtil;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.*;

@DisplayName("GQL_Изменение фото")
public class UpdatePhotoGqlTest extends BaseGraphQlTest {

    private final PhotoApi photoApi = new PhotoApi(apolloClient);
    private final FeedApi feedApi = new FeedApi(apolloClient);


    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "old")})
    @ApiLogin
    @DisplayName("Обновление фото: передан только id — данные не меняются")
    void updatePhotoIdOnlyShouldChangeNothing(@Token String bearerToken, AppUser user) {

        // Подготовка: берём моё фото и текущее состояние
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();
        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = PhotoUtil.findPhotoById(before, photoId);

        final String beforeDesc = beforeNode.description;
        final String beforeCountry = beforeNode.country.code;
        final String beforeSrc = beforeNode.src;

        // Действие: отправляем update только с id
        UpdatePhotoMutation.Data res = photoApi.updatePhoto(bearerToken, photoId, null, null, null);

        assertNotNull(res.photo, "photo in response must be non-null");
        assertEquals(photoId, res.photo.id, "id must remain unchanged");

        // Пост-проверка: все поля остались прежними
        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = PhotoUtil.findPhotoById(after, photoId);

        assertAll("no changes when only id passed",
                () -> assertEquals(beforeDesc, afterNode.description),
                () -> assertEquals(beforeCountry, afterNode.country.code),
                () -> assertNotNull(afterNode.src),
                () -> assertFalse(afterNode.src.isEmpty()),
                () -> assertEquals(beforeSrc, afterNode.src)
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "before", count = 1)})
    @ApiLogin
    @DisplayName("Обновление фото: меняется только описание — страна и src не меняются")
    void updatePhotoShouldKeepCountryAndSrcWhenOnlyDescriptionChanged(@Token String bearerToken, AppUser user) {

        // Подготовка: берём моё фото и текущее состояние
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();

        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = PhotoUtil.findPhotoById(before, photoId);

        final String beforeCountry = beforeNode.country.code;
        final String beforeSrc = beforeNode.src;
        final String newDescription = "after-desc";

        // Действие: отправляем update с изменением только описания
        UpdatePhotoMutation.Data res = photoApi.updatePhoto(
                bearerToken,
                photoId,
                null,
                newDescription,
                null);

        assertNotNull(res.photo);
        assertEquals(photoId, res.photo.id);

        // Пост-проверка: описание изменилось, остальные остались
        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = PhotoUtil.findPhotoById(after, photoId);

        assertAll("only description changed",
                () -> assertEquals(newDescription, afterNode.description),
                () -> assertEquals(beforeCountry, afterNode.country.code, "country must remain unchanged"),
                () -> assertNotNull(afterNode.src),
                () -> assertFalse(afterNode.src.isEmpty()),
                () -> assertEquals(beforeSrc, afterNode.src, "src must remain unchanged")
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "old-desc", count = 1)})
    @ApiLogin
    @DisplayName("Обновление фото: меняем только страну — описание и src не меняются")
    void updatePhotoShouldKeepDescriptionAndSrcWhenOnlyCountryChanged(@Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();

        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = PhotoUtil.findPhotoById(before, photoId);
        final String beforeDesc = beforeNode.description;
        final String beforeSrc = beforeNode.src;

        // Действие: отправляем update с изменением только страны
        final String newCountry = "it";
        UpdatePhotoMutation.Data res = photoApi.updatePhoto(bearerToken, photoId, null, null, newCountry);
        assertNotNull(res.photo);
        assertEquals(photoId, res.photo.id);

        // Пост-проверка: страна изменилась, остальные остались
        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = PhotoUtil.findPhotoById(after, photoId);

        assertAll("only country changed",
                () -> assertEquals(newCountry, afterNode.country.code),
                () -> assertEquals(beforeDesc, afterNode.description, "description must remain unchanged"),
                () -> assertNotNull(afterNode.src),
                () -> assertFalse(afterNode.src.isEmpty()),
                () -> assertEquals(beforeSrc, afterNode.src, "src must remain unchanged")
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "keep-desc", count = 1)})
    @ApiLogin
    @DisplayName("Обновление фото: меняем только src (dataURL) — описание и страна не меняются")
    void updatePhotoShouldKeepDescriptionAndCountryWhenOnlySrcChanged(@Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();

        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = PhotoUtil.findPhotoById(before, photoId);
        final String beforeDesc = beforeNode.description;
        final String beforeCountry = beforeNode.country.code;
        final String beforeSrc = beforeNode.src;

        // Действие: отправляем update с изменением только фото
        UpdatePhotoMutation.Data res = photoApi.updatePhoto(
                bearerToken,
                photoId,
                ImageDataUrl.photo_png2,
                null,
                null);

        assertNotNull(res.photo);
        assertEquals(photoId, res.photo.id);
        assertNotNull(res.photo.src);
        assertFalse(res.photo.src.isEmpty());

        // Пост-проверка: фото изменилось, остальные остались
        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = PhotoUtil.findPhotoById(after, photoId);

        assertAll("only src changed",
                () -> assertEquals(beforeDesc, afterNode.description, "description must remain unchanged"),
                () -> assertEquals(beforeCountry, afterNode.country.code, "country must remain unchanged"),
                () -> assertNotNull(afterNode.src),
                () -> assertFalse(afterNode.src.isEmpty()),
                () -> assertNotEquals(beforeSrc, afterNode.src, "src must change")
        );
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "old description", count = 1)})
    @ApiLogin
    @DisplayName("Обновление фото: пользователь меняет описание и страну, id и src сохраняются")
    void updatePhotoWhenOwnerShouldChangeFieldsAndKeepId(@Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();
        final String newDescription = "new description";
        final String newCountry = "it";

        UpdatePhotoMutation.Data updated = photoApi.updatePhoto(bearerToken, photoId, null, newDescription, newCountry);

        assertAll("updated photo fields",
                () -> assertNotNull(updated.photo, "photo must be non-null"),
                () -> assertEquals(photoId, updated.photo.id, "id must remain unchanged"),
                () -> assertEquals(newDescription, updated.photo.description, "description must be updated"),
                () -> assertNotNull(updated.photo.country, "country must be present"),
                () -> assertFalse(updated.photo.src.isEmpty(), "src in feed must be non-empty"),
                () -> assertEquals(newCountry, updated.photo.country.code, "country.code must be updated")
        );

        GetFeedQuery.Data afterRes = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node photo = PhotoUtil.findPhotoById(afterRes, photoId);

        assertAll("feed reflects updates",
                () -> assertEquals(newDescription, photo.description),
                () -> assertEquals(newCountry, photo.country.code),
                () -> assertNotNull(photo.src),
                () -> assertFalse(photo.src.isEmpty())
        );
    }

    @Test
    @User(friends = 1,
            photos = {
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "de", description = "friend description", count = 1)})
    @ApiLogin
    @DisplayName("Обновление чужого фото: отказ по доступу, изменения не применяются")
    void updatePhotoShouldFailForbiddenWhenUpdatingForeignPhoto(@Token String bearerToken, AppUser user) {
        final UUID friendId = AnnotationHelper.firstFriendId(user);
        final UUID friendPhotoId = AnnotationHelper.firstPhotoId(user, friendId);
        final String newDescription = "it should not be saved";

        ApolloResponse<UpdatePhotoMutation.Data> tryUpdateRes =
                photoApi.tryUpdatePhoto(bearerToken, friendPhotoId.toString(), null, newDescription, null);

        assertAll("forbidden update",
                () -> assertTrue(tryUpdateRes.hasErrors()),
                () -> assertEquals("FORBIDDEN", ErrorGql.classification(tryUpdateRes)),
                () -> assertEquals("photo", ErrorGql.path(tryUpdateRes))
        );

        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, true);
        GetFeedQuery.Node node = PhotoUtil.findPhotoById(after, friendPhotoId.toString());

        assertNotEquals(newDescription, node.description, "friend's photo must not be changed");
    }

    @Test
    @User(photos = {@Photo(countryCode = "fr", description = "my description", count = 1)})
    @ApiLogin
    @DisplayName("Обновление без авторизации: UNAUTHORIZED, данные не меняются")
    void updatePhotoWithoutAuthShouldFailForbidden(@Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();
        GetFeedQuery.Data before = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node beforeNode = PhotoUtil.findPhotoById(before, photoId);

        ApolloResponse<UpdatePhotoMutation.Data> tryUpdateRes =
                photoApi.tryUpdatePhotoWithoutAuth(photoId, null, "не сохранится", null);

        assertAll("unauthorized update",
                () -> assertTrue(tryUpdateRes.hasErrors()),
                () -> assertEquals("FORBIDDEN", ErrorGql.classification(tryUpdateRes)),
                () -> assertEquals("photo", ErrorGql.path(tryUpdateRes), "error path must contain 'photo'")

        );

        GetFeedQuery.Data after = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node afterNode = PhotoUtil.findPhotoById(after, photoId);

        assertEquals(beforeNode.description, afterNode.description,
                "description must remain unchanged after unauthorized attempt");
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("Обновление несуществующего id: NOT_FOUND")
    void updatePhotoShouldReturnNotFoundWhenIdDoesNotExist(@Token String bearerToken) {
        final String randomId = UUID.randomUUID().toString();

        ApolloResponse<UpdatePhotoMutation.Data> tryUpdateRes =
                photoApi.tryUpdatePhoto(bearerToken, randomId, null, "", null);

        assertAll("not found",
                () -> assertTrue(tryUpdateRes.hasErrors()),
                () -> assertEquals("NOT_FOUND", ErrorGql.classification(tryUpdateRes)),
                () -> assertEquals("Can't find photo by id: " + randomId, ErrorGql.message(tryUpdateRes)),
                () -> assertEquals("photo", ErrorGql.path(tryUpdateRes), "error path must contain 'photo'")
        );
    }

    @ParameterizedTest(name = "Разрешённый формат: {0}")
    @MethodSource("allowedFormats")
    @User(photos = { @Photo(countryCode = "fr", description = "ok formats", count = 1) })
    @ApiLogin
    @DisplayName("Обновление фото (параметризованный): разрешённые форматы принимаются")
    void updatePhotoWithAllowedFormatsShouldSucceed(String label, String dataUrl, @Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();

        UpdatePhotoMutation.Data res = photoApi.updatePhotoFromClasspath(bearerToken, photoId, dataUrl, null, null);

        assertAll("allowed format: " + label,
                () -> assertNotNull(res.photo),
                () -> assertEquals(photoId, res.photo.id),
                () -> assertNotNull(res.photo.src),
                () -> assertFalse(res.photo.src.isEmpty(), "src must be non-empty")
        );

        GetFeedQuery.Data feed = feedApi.getFeed(bearerToken, 0, 10, false);
        GetFeedQuery.Node node = PhotoUtil.findPhotoById(feed, photoId);

        assertAll("feed after allowed format update: " + label,
                () -> assertNotNull(node.src),
                () -> assertFalse(node.src.isEmpty())
        );
    }

    @ParameterizedTest(name = "Неразрешённый формат: {0}")
    @MethodSource("unsupportedFormats")
    @User(photos = { @Photo(countryCode = "fr", description = "bad formats") })
    @ApiLogin
    @DisplayName("Обновление фото (параметризованный): неразрешённые форматы → BAD_REQUEST, UNSUPPORTED_IMAGE_FORMAT")
    void updatePhotoWithUnsupportedFormatsShouldFailBadRequest(String label, String dataUrl, @Token String bearerToken, AppUser user) {
        final String photoId = AnnotationHelper.firstMyPhotoId(user).toString();

        ApolloResponse<UpdatePhotoMutation.Data> resp =
                photoApi.tryUpdatePhoto(bearerToken, photoId, dataUrl, null, null);

        assertAll("unsupported format: " + label,
                () -> assertTrue(resp.hasErrors(), "response must contain errors"),
                () -> assertEquals("BAD_REQUEST", ErrorGql.classification(resp), "classification must be BAD_REQUEST"),
                () -> assertEquals("Unsupported image format. Allowed: image/jpeg, image/png, image/gif, image/webp",
                        ErrorGql.message(resp)),
                () -> assertEquals("photo", ErrorGql.path(resp), "error path must be 'photo'")
        );
    }

    // ---------- providers ----------
    private static java.util.stream.Stream<Arguments> allowedFormats() {
        // разрешённые форматы: png, jpeg, gif, webp
        return java.util.stream.Stream.of(
                Arguments.of("png", "avatar/3.png"),
                Arguments.of("jpeg", "avatar/2.jpg"),
              Arguments.of("gif", "avatar/4.gif"),
                Arguments.of("webp","avatar/3.webp")
        );
    }

    private static java.util.stream.Stream<Arguments> unsupportedFormats() {
        return java.util.stream.Stream.of(
                Arguments.of("pdf", photo_pdf),
                Arguments.of("x-icon", photo_x_icon)
        );
    }
}

