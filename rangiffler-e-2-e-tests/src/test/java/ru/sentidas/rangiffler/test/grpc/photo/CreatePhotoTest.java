package ru.sentidas.rangiffler.test.grpc.photo;

import com.codeborne.selenide.Selenide;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.CreatePhotoRequest;
import ru.sentidas.rangiffler.grpc.PhotoPageRequest;
import ru.sentidas.rangiffler.grpc.PhotoResponse;
import ru.sentidas.rangiffler.grpc.PhotosPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.*;

@GrpcTest
@DisplayName("Grpc_Photo: createPhoto")
public class CreatePhotoTest extends BaseTest {

    @Test
    @User
    @DisplayName("Создание фото PNG: total увеличился на 1, фото первое, поля совпадают")
    public void createPhotoPlacesFirstAndIncrementsTotalWhenPngUploaded(AppUser user) {
        final int pageSize = 7;
        final String expectedCountryCode = "fr";
        final String expectedDescription = "png created";

        PhotosPageResponse photosPageBefore = userPhotosPage(user, 0, pageSize, true);

        PhotoResponse createdPhoto = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setCountryCode(expectedCountryCode)
                        .setSrc(DATA_URL_PNG)
                        .setDescription(expectedDescription)
                        .build()
        );

        PhotosPageResponse photosPageAfter = userPhotosPage(user, 0, pageSize, true);
        PhotoResponse firstPhotoAfter = photosPageAfter.getContent(0);

        assertAll("created photo appears first and counters are correct",
                () -> assertEquals(photosPageBefore.getTotal() + 1, photosPageAfter.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(createdPhoto.getPhotoId(), firstPhotoAfter.getPhotoId(),
                        "first photo should be the created one"),

                // photoId presence
                () -> assertNotNull(firstPhotoAfter.getPhotoId(), "photoId must not be null"),
                () -> assertFalse(firstPhotoAfter.getPhotoId().isBlank(), "photoId must not be blank"),

                // src with required extension
                () -> assertNotNull(firstPhotoAfter.getSrc(), "src must not be null"),
                () -> assertTrue(firstPhotoAfter.getSrc().toLowerCase().endsWith(".png"),
                        "src should end with .png"),

                // country & description match expectations
                () -> assertEquals(expectedCountryCode, firstPhotoAfter.getCountryCode(),
                        "countryCode should match expected"),
                () -> assertEquals(expectedDescription, firstPhotoAfter.getDescription(),
                        "description should match")
        );
    }

    @Test
    @User
    @DisplayName("Создание JPEG: total увеличился на 1, фото первое")
    public void createPhotoPlacesFirstAndIncrementsTotalWhenJpegUploaded(AppUser user) {
        final int pageSize = 3;
        final String expectedDescription = "jpeg created";
        final String expectedCountryCode = "fr";

        PhotosPageResponse before = userPhotosPage(user, 0, pageSize, true);

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_JPEG)
                        .setCountryCode(expectedCountryCode)
                        .setDescription(expectedDescription)
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, pageSize, true);
        PhotoResponse firstPhoto = after.getContent(0);

        assertAll("created jpeg photo appears first and fields match",
                () -> assertEquals(before.getTotal() + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), firstPhoto.getPhotoId(),
                        "first photo should be the created one"),

                // photoId presence
                () -> assertNotNull(firstPhoto.getPhotoId(), "photoId must not be null"),
                () -> assertFalse(firstPhoto.getPhotoId().isBlank(), "photoId must not be blank"),

                // src with expected extension (.jpg or .jpeg)
                () -> assertNotNull(firstPhoto.getSrc(), "src must not be null"),
                () -> assertFalse(firstPhoto.getSrc().isBlank(), "src must not be blank"),
                () -> assertTrue(firstPhoto.getSrc().toLowerCase().matches(".*\\.(jpg|jpeg)$"),
                        "src should end with .jpg or .jpeg"),

                // country & description match
                () -> assertEquals(expectedCountryCode, firstPhoto.getCountryCode(),
                        "countryCode should match expected"),
                () -> assertEquals(expectedDescription, firstPhoto.getDescription(),
                        "description should match")
        );
    }

    @Test
    @User
    @DisplayName("Создание GIF: total увеличился на 1, фото первое")
    public void createPhotoPlacesFirstAndIncrementsTotalWhenGifUploaded(AppUser user) {
        final int pageSize = 5;
        final String expectedCountryCode = "it";
        final String expectedDescription = "gif created";

        PhotosPageResponse before = userPhotosPage(user, 0, pageSize, true);
        final int totalBefore = before.getTotal();

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_GIF)
                        .setCountryCode(expectedCountryCode)
                        .setDescription(expectedDescription)
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, pageSize, true);
        PhotoResponse firstPhoto = after.getContent(0);

        assertAll("created gif photo appears first and fields match",
                () -> assertEquals(totalBefore + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), firstPhoto.getPhotoId(),
                        "first photo should be the created one"),

                // photoId presence
                () -> assertNotNull(firstPhoto.getPhotoId(), "photoId must not be null"),
                () -> assertFalse(firstPhoto.getPhotoId().isBlank(), "photoId must not be blank"),

                // src with expected extension (.gif)
                () -> assertNotNull(firstPhoto.getSrc(), "src must not be null"),
                () -> assertFalse(firstPhoto.getSrc().isBlank(), "src must not be blank"),
                () -> assertTrue(firstPhoto.getSrc().toLowerCase().endsWith(".gif"),
                        "src should end with .gif"),

                // country & description match
                () -> assertEquals(expectedCountryCode, firstPhoto.getCountryCode(),
                        "countryCode should match expected"),
                () -> assertEquals(expectedDescription, firstPhoto.getDescription(),
                        "description should match")
        );
    }

    @Test
    @User
    @DisplayName("Создание WEBP: total увеличился на 1, фото первое")
    public void createPhotoPlacesFirstAndIncrementsTotalWhenWebpUploaded(AppUser user) {
        final int pageSize = 5;
        final String expectedCountryCode = "es";
        final String expectedDescription = "webp created";

        PhotosPageResponse before = userPhotosPage(user, 0, pageSize, true);
        final int totalBefore = before.getTotal();

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_WEBP)
                        .setCountryCode(expectedCountryCode)
                        .setDescription(expectedDescription)
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, pageSize, true);
        PhotoResponse firstPhoto = after.getContent(0);

        assertAll("created webp photo appears first and fields match",
                () -> assertEquals(totalBefore + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), firstPhoto.getPhotoId(),
                        "first photo should be the created one"),

                // photoId presence
                () -> assertNotNull(firstPhoto.getPhotoId(), "photoId must not be null"),
                () -> assertFalse(firstPhoto.getPhotoId().isBlank(), "photoId must not be blank"),

                // src with expected extension (.webp)
                () -> assertNotNull(firstPhoto.getSrc(), "src must not be null"),
                () -> assertFalse(firstPhoto.getSrc().isBlank(), "src must not be blank"),
                () -> assertTrue(firstPhoto.getSrc().toLowerCase().endsWith(".webp"),
                        "src should end with .webp"),

                // country & description match
                () -> assertEquals(expectedCountryCode, firstPhoto.getCountryCode(),
                        "countryCode should match expected"),
                () -> assertEquals(expectedDescription, firstPhoto.getDescription(),
                        "description should match")
        );
    }

    @Test
    @User
    @DisplayName("Нормализация кода страны: вход FR → хранение/выдача fr")
    public void countryCodeNormalizesToLowercaseWhenUppercaseProvidedOnCreate(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 12, true);
        final int totalBefore = before.getTotal();

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_PNG)
                        .setCountryCode("FR") // верхний регистр на входе
                        .setDescription("country upper")
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, 12, true);
        PhotoResponse first = after.getContent(0);

        assertAll("country code normalization and total increment",
                () -> assertEquals(totalBefore + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), first.getPhotoId(),
                        "first photo should be the created one"),
                () -> assertEquals("fr", first.getCountryCode(),
                        "countryCode should be normalized to lowercase")
        );
    }

    @Test
    @User
    @DisplayName("Лайки по умолчанию при создании фото: total=0, список пуст")
    public void initializeLikesToZeroWhenPhotoCreated(AppUser user) {
        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_PNG)
                        .setCountryCode("fr")
                        .setDescription("likes default")
                        .build()
        );

        PhotosPageResponse page0 = userPhotosPage(user, 0, 10, true);
        PhotoResponse first = page0.getContent(0);

        assertAll("likes initialized to zero",
                () -> assertEquals(created.getPhotoId(), first.getPhotoId(),
                        "first photo should be the created one"),
                () -> assertNotNull(first.getLikes(), "likes must not be null"),
                () -> assertEquals(0, first.getLikes().getTotal(), "likes.total should be 0"),
                () -> assertEquals(0, first.getLikes().getLikesCount(), "likes.likesCount should be 0")
        );
    }

    @Test
    @User
    @DisplayName("Длинное описание сохраняется полностью и фото первое")
    public void persistLongDescriptionAndPlacePhotoFirstWhenCreated(AppUser user) {
        final String longDescription = DATA_URL;

        PhotosPageResponse before = userPhotosPage(user, 0, 12, true);
        final int totalBefore = before.getTotal();

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL)
                        .setCountryCode("fr")
                        .setDescription(longDescription)
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, 12, true);
        PhotoResponse first = after.getContent(0);

        assertAll("long description persisted and photo is first",
                () -> assertEquals(totalBefore + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), first.getPhotoId(),
                        "first photo should be the created one"),
                () -> assertEquals(longDescription, first.getDescription(),
                        "description should match the long input")
        );
    }

    @Test
    @User
    @DisplayName("Два создания подряд: второе фото выше первого")
    public void secondCreatedPhotoPrecedesFirstWhenSortedByCreationDate(AppUser user) throws InterruptedException {
        final PhotosPageResponse before = userPhotosPage(user, 0, 50, true);
        final int totalBefore = before.getTotal();

        PhotoResponse firstCreated = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_WEBP)
                        .setCountryCode("es")
                        .setDescription("webp created #1")
                        .build()
        );

        Thread.sleep(2000); //  пауза, чтобы гарантировать различие creationDate

        PhotoResponse secondCreated = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_WEBP)
                        .setCountryCode("es")
                        .setDescription("webp created #2")
                        .build()
        );

        PhotosPageResponse page0 = userPhotosPage(user, 0, 12, true);

        assertAll("second created should appear above the first",
                () -> assertEquals(totalBefore + 2, page0.getTotal(),
                        "total should increase by two"),
                () -> assertEquals(secondCreated.getPhotoId(), page0.getContent(0).getPhotoId(),
                        "second created should be first"),
                () -> assertEquals(firstCreated.getPhotoId(), page0.getContent(1).getPhotoId(),
                        "first created should be second")
        );
    }

    @Test
    @User
    @DisplayName("Пустое описание допустимо: фото создаётся и первое")
    public void createPhotoWithEmptyDescriptionPlacesFirstWhenCreated(AppUser user) {
        PhotosPageResponse before = userPhotosPage(user, 0, 12, true);
        final int totalBefore = before.getTotal();

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setCountryCode("fr")
                        .setSrc(DATA_URL_PNG)
                        .build()
        );

        PhotosPageResponse after = userPhotosPage(user, 0, 12, true);
        PhotoResponse first = after.getContent(0);

        assertAll("empty description allowed and photo is first",
                () -> assertEquals(totalBefore + 1, after.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), first.getPhotoId(),
                        "first photo should be the created one"),
                () -> assertEquals("", first.getDescription(),
                        "description should be empty string")
        );
    }

    @Test
    @DisplayName("Пагинация: новое фото вытесняет хвост первой страницы")
    @User(photos = {
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p1"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p2"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p3"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p4")
    })
    public void pushTailItemToNextPageWhenNewPhotoAppearsOnFirstPage(AppUser user) {
        final int pageSize = 3;

       // На стр. 0 - 3 фото; на стр. 1 - 1 фото
        PhotosPageResponse before = userPhotosPage(user, 0, pageSize, true);
        assertEquals(pageSize, before.getContentCount(), "page 1 content length should equal requested size");

        // Запоминаем «хвост» первой страницы (последний элемент page 0)
        // — он должен переехать на страницу 2 после добавления нового фото
        final String previousTailPhotoId = before.getContent(pageSize - 1).getPhotoId();
        final int totalBefore = before.getTotal();

        Selenide.sleep(1000);

        PhotoResponse created = photoBlockingStub.createPhoto(
                CreatePhotoRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setSrc(DATA_URL_JPEG)
                        .setCountryCode("fr")
                        .setDescription("new-on-first")
                        .build()
        );

        // После создания нового фото: оно должно стать первым на странице 0,
        // а прежний хвост — уехать на страницу 1.
        PhotosPageResponse firstPageAfter = userPhotosPage(user, 0, pageSize, true);
        PhotosPageResponse secondPageAfter = userPhotosPage(user, 1, pageSize, true);

        assertAll("new photo becomes first and previous tail moves to page 2",
                () -> assertEquals(totalBefore + 1, firstPageAfter.getTotal(),
                        "total should increase by one"),
                () -> assertEquals(created.getPhotoId(), firstPageAfter.getContent(0).getPhotoId(),
                        "new photo should be first on page 1"),
                () -> assertEquals(pageSize, firstPageAfter.getContentCount(),
                        "page 1 content length should still equal requested size"),
                () -> assertTrue(containsPhotoId(secondPageAfter, previousTailPhotoId),
                        "previous tail item should be present on page 2"),
                () -> assertTrue(firstPageAfter.getContentList().stream()
                                .noneMatch(p -> p.getPhotoId().equals(previousTailPhotoId)),
                        "previous tail item should no longer be on page 1")
        );
    }

    @Test
    @User(photo = 7)
    @DisplayName("Постраничная выдача: все поля ответа корректны на трёх страницах (size=2)")
    public void verifyAllResponseFieldsAcrossPages(AppUser user) {
        final int expectedTotalPhotosAcrossAllPages = 7;
        final int requestedPageSize = 2;

        // page 0
        PhotosPageResponse page0 = userPhotosPage(user, 0, requestedPageSize, true);
        assertAll("page 0 metadata",
                () -> assertEquals(expectedTotalPhotosAcrossAllPages, page0.getTotal(), "total should match expected total photos"),
                () -> assertEquals(requestedPageSize, page0.getSize(), "page size should match requested size"),
                () -> assertEquals(page0.getContentCount(), page0.getTotalElements(), "totalElements should equal content length"),
                () -> assertTrue(page0.getContentCount() == 2, "length content should be equals requestedPageSize"),
                () -> assertTrue(page0.getFirst(), "first must be true on the first page")
        );
        for (int i = 0; i < page0.getContentCount(); i++) {
            PhotoResponse photo = page0.getContent(i);
            assertAll("page 0 content[" + i + "] contract",
                    () -> assertNotNull(photo.getPhotoId(), "photoId must not be null"),
                    () -> assertEquals(user.id().toString(), photo.getUserId(), "userId should equal requesting user id"),
                    () -> assertNotNull(photo.getSrc(), "src must not be null"),
                    () -> assertFalse(photo.getSrc().isBlank(), "src must not be blank"),
                    () -> assertNotNull(photo.getCountryCode(), "countryCode must not be null"),
                    () -> assertTrue(photo.getCountryCode().matches("^[a-z]{2}$"), "countryCode should be 2-letter lowercase ISO code"),
                    () -> assertNotNull(photo.getDescription(), "description must not be null"),
                    () -> assertFalse(photo.getDescription().isBlank(), "description must not be blank"),
                    () -> assertNotNull(photo.getCreationDate(), "creationDate must not be null"),
                    () -> assertNotNull(photo.getLikes(), "likes must not be null")
            );
        }

        // page 1
        PhotosPageResponse page1 = userPhotosPage(user, 1, requestedPageSize, true);
        assertAll("page 1 metadata",
                () -> assertEquals(expectedTotalPhotosAcrossAllPages, page1.getTotal(), "total should match expected total photos"),
                () -> assertEquals(requestedPageSize, page1.getSize(), "page size should match requested size"),
                () -> assertEquals(page1.getContentCount(), page1.getTotalElements(), "totalElements should equal content length"),
                () -> assertTrue(page1.getContentCount() == 2, "length content should be equals requestedPageSize"),
                () -> assertFalse(page1.getFirst(), "first must be false on non-first pages"),
                () -> assertEquals(1, page1.getPage(), "page index should be 1")
        );
        for (int i = 0; i < page1.getContentCount(); i++) {
            PhotoResponse photo = page1.getContent(i);
            assertAll("page 1 content[" + i + "] contract",
                    () -> assertNotNull(photo.getPhotoId(), "photoId must not be null"),
                    () -> assertEquals(user.id().toString(), photo.getUserId(), "userId should equal requesting user id"),
                    () -> assertNotNull(photo.getSrc(), "src must not be null"),
                    () -> assertFalse(photo.getSrc().isBlank(), "src must not be blank"),
                    () -> assertNotNull(photo.getCountryCode(), "countryCode must not be null"),
                    () -> assertTrue(photo.getCountryCode().matches("^[a-z]{2}$"), "countryCode should be 2-letter lowercase ISO code"),
                    () -> assertNotNull(photo.getDescription(), "description must not be null"),
                    () -> assertFalse(photo.getDescription().isBlank(), "description must not be blank"),
                    () -> assertNotNull(photo.getCreationDate(), "creationDate must not be null"),
                    () -> assertNotNull(photo.getLikes(), "likes must not be null")
            );
        }

        // page 2
        PhotosPageResponse page2 = userPhotosPage(user, 2, requestedPageSize, true);
        assertAll("page 2 metadata",
                () -> assertEquals(expectedTotalPhotosAcrossAllPages, page2.getTotal(), "total should match expected total photos"),
                () -> assertEquals(requestedPageSize, page2.getSize(), "page size should match requested size"),
                () -> assertEquals(page2.getContentCount(), page2.getTotalElements(), "totalElements should equal content length"),
                () -> assertTrue(page2.getContentCount() == 2, "length content should be equals requestedPageSize"),
                () -> assertFalse(page2.getFirst(), "first must be false on non-first pages"),
                () -> assertEquals(2, page2.getPage(), "page index should be 2")
        );
        for (int i = 0; i < page2.getContentCount(); i++) {
            PhotoResponse photo = page2.getContent(i);
            assertAll("page 2 content[" + i + "] contract",
                    () -> assertNotNull(photo.getPhotoId(), "photoId must not be null"),
                    () -> assertEquals(user.id().toString(), photo.getUserId(), "userId should equal requesting user id"),
                    () -> assertNotNull(photo.getSrc(), "src must not be null"),
                    () -> assertFalse(photo.getSrc().isBlank(), "src must not be blank"),
                    () -> assertNotNull(photo.getCountryCode(), "countryCode must not be null"),
                    () -> assertTrue(photo.getCountryCode().matches("^[a-z]{2}$"), "countryCode should be 2-letter lowercase ISO code"),
                    () -> assertNotNull(photo.getDescription(), "description must not be null"),
                    () -> assertFalse(photo.getDescription().isBlank(), "description must not be blank"),
                    () -> assertNotNull(photo.getCreationDate(), "creationDate must not be null"),
                    () -> assertNotNull(photo.getLikes(), "likes must not be null")
            );
        }
    }

    // ==== Негативные сценарии (валидация gRPC) ====

    @Test
    @DisplayName("Невалидный user_id: INVALID_ARGUMENT")
    public void invalidUserId_invalidArgument() {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId("not-a-uuid")
                                .setSrc(DATA_URL_PNG)
                                .setCountryCode("fr")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }


    @Test
    @DisplayName("Пустой src: INVALID_ARGUMENT")
    @User
    public void emptySrc_invalidArgument(AppUser user) {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setSrc("")
                                .setCountryCode("fr")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @DisplayName("src не data:-URL: INVALID_ARGUMENT")
    @User
    public void notDataUrl_invalidArgument(AppUser user) {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setSrc(NOT_DATA_URL)
                                .setCountryCode("fr")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @DisplayName("Битая base64 в data:-URL: INVALID_ARGUMENT")
    @User
    public void brokenBase64_invalidArgument(AppUser user) {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setSrc(DATA_URL_BROKEN_BASE64)
                                .setCountryCode("fr")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @DisplayName("Пустой country_code: INVALID_ARGUMENT")
    @User
    public void emptyCountry_invalidArgument(AppUser user) {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setSrc(DATA_URL_PNG)
                                .setCountryCode("")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @DisplayName("country_code не ISO-2: INVALID_ARGUMENT")
    @User
    public void invalidCountryFormat_invalidArgument(AppUser user) {
        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.createPhoto(
                        CreatePhotoRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setSrc(DATA_URL_PNG)
                                .setCountryCode("FRA")
                                .setDescription("desc")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    // ---------- Helpers ----------

    private PhotosPageResponse userPhotosPage(AppUser user, int page, int size, boolean includeTotal) {

        return photoBlockingStub.getUserPhotos(
                PhotoPageRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setPage(page)
                        .setSize(size)
                        .setIncludeTotal(includeTotal)
                        .build()
        );
    }

    private static boolean containsPhotoId(PhotosPageResponse response, String photoId) {
        for (PhotoResponse pr : response.getContentList()) {
            if (pr.getPhotoId().equals(photoId)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isObjectStorage() {
        String v = System.getProperty("app.media.storage.default",
                System.getenv().getOrDefault("APP_MEDIA_STORAGE_DEFAULT", "OBJECT"));
        return "OBJECT".equalsIgnoreCase(v);
    }

}
