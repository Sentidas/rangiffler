package ru.sentidas.rangiffler.test.gql.mutation.photo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.CreatePhotoMutation;
import ru.sentidas.GetFeedQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.FeedApi;
import ru.sentidas.rangiffler.test.gql.api.PhotoApi;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.test.gql.support.PhotoUtil.findPhotoById;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL;

@DisplayName("GQL_Создание фото")
public class CreatePhotoTest extends BaseGraphQlTest {

    private final PhotoApi photoApi = new PhotoApi(apolloClient);
    private final FeedApi feedApi = new FeedApi(apolloClient);

    @Test
    @User
    @ApiLogin
    @DisplayName("Создание фото: корректные поля и нулевые лайки")
    void createPhotoShouldReturnValidPhotoWithZeroLikesWhenInputIsValid(@Token String bearerToken) {
        final String description = "Париж, я люблю тебя";
        final String countryCode = "fr";

        // создание фото
        CreatePhotoMutation.Data created = photoApi.createPhoto(bearerToken, DATA_URL, description, countryCode);

        assertAll("photo fields",
                () -> assertNotNull(created.photo, "photo must be non-null"),
                () -> assertNotNull(created.photo.src, "src must match input"),
                () -> assertEquals(description, created.photo.description, "description must match input"),
                () -> assertNotNull(created.photo.country, "country must be present"),
                () -> assertEquals(countryCode, created.photo.country.code, "country.code must match input"),
                () -> assertNotNull(created.photo.country.name, "country.name must be present"),
                () -> assertFalse(created.photo.country.name.isEmpty(), "country.name must be non-empty"),
                () -> assertNotNull(created.photo.country.flag, "country.flag must be present"),
                () -> assertFalse(created.photo.country.flag.isEmpty(), "country.flag must be non-empty"),
                () -> assertNotNull(created.photo.likes, "likes block must be present"),
                () -> assertEquals(0, created.photo.likes.total, "likes.total must be 0 on create")
        );

        // запрос ленты для проверки созданного фото
        GetFeedQuery.Data feedRes = feedApi.getFeed(bearerToken, 0, 20, false);
        GetFeedQuery.Node photo = findPhotoById(feedRes, created.photo.id);

        assertAll("in my feed",
                () -> assertNotNull(photo, "photo must be present in my feed"),
                () -> assertEquals(created.photo.id, photo.id, "ids must match"),
                () -> assertFalse(photo.src.isEmpty(), "src in feed must be non-empty")
        );
    }
}
