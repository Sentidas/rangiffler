package ru.sentidas.rangiffler.test.grpc.photo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.PhotoPageRequest;
import ru.sentidas.rangiffler.grpc.PhotosPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.friendId;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL_PNG;

@GrpcTest
@DisplayName("Photo: getUser/FeedPhoto")
public class GetPhotosTest extends BaseTest {

    // ===== GetUserPhotos (мои фото) ======

    @Test
    @DisplayName("GetUserPhotos: порядок фото по дате убывания, include_total=true, первая страница валидна")
    @User(photos = {
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p1"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p2"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p3")
    })
    public void getUserPhotosOrdersByCreationDescAndReturnsTotalWhenIncludeTotalTrue(AppUser user) {
        final PhotosPageResponse page0 = userPhotosPage(user, 0, 10, true);

        assertAll("first page metadata and order",
                () -> assertTrue(page0.hasTotal(), "total should be present when include_total=true"),
                () -> assertEquals(3, page0.getTotal(), "total should equal 3"),
                () -> assertEquals(3, page0.getContentCount(), "content count should equal 3"),
                () -> assertTrue(page0.getFirst(), "first flag should be true"),
                () -> assertTrue(page0.getLast(), "last flag should be true when all fit into one page")
        );

        // Порядок по дате
        final String idFirst = page0.getContent(0).getPhotoId();
        final String idSecond = page0.getContent(1).getPhotoId();
        final String idThird = page0.getContent(2).getPhotoId();

        assertAll("order photo content",
                () -> assertNotNull(idFirst, "first id must not be null"),
                () -> assertNotNull(idSecond, "second id must not be null"),
                () -> assertNotNull(idThird, "third id must not be null"),
                () -> assertNotEquals(idFirst, idSecond, "first and second must differ"),
                () -> assertNotEquals(idSecond, idThird, "second and third must differ")
        );
    }

    @Test
    @DisplayName("GetUserPhotos: пагинация и флаги first/last, include_total=false (total отсутствует)")
    @User(photos = {
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p1"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p2"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p3")
    })
    public void getUserPhotosSetsFirstLastFlagsAndOmitsTotalWhenIncludeTotalFalse(AppUser user) {
        final PhotosPageResponse page0 = userPhotosPage(user, 0, 2, false);
        final PhotosPageResponse page1 = userPhotosPage(user, 1, 2, false);

        assertAll("page 0",
                () -> assertFalse(page0.hasTotal(), "total should be absent when include_total=false"),
                () -> assertEquals(2, page0.getContentCount(), "content count should equal 2"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertFalse(page0.getLast(), "last should be false")
        );

        assertAll("page 1",
                () -> assertFalse(page1.hasTotal(), "total should be absent on page 1 too"),
                () -> assertEquals(1, page1.getContentCount(), "content count should equal 1"),
                () -> assertFalse(page1.getFirst(), "first should be false"),
                () -> assertTrue(page1.getLast(), "last should be true")
        );
    }

    @Test
    @DisplayName("GetUserPhotos: пустая лента (content пуст, first=true,last=true, total=0)")
    @User
    public void getUserPhotosReturnsEmptyPageWhenNoPhotos(AppUser user) {
        final PhotosPageResponse page0 = userPhotosPage(user, 0, 10, true);

        assertAll("empty page",
                () -> assertTrue(page0.hasTotal(), "total should be present"),
                () -> assertEquals(0, page0.getTotal(), "total should be 0"),
                () -> assertEquals(0, page0.getContentCount(), "content count should be 0"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertTrue(page0.getLast(), "last should be true")
        );
    }


    @Test
    @DisplayName("GetUserPhotos: невалидный user_id → INVALID_ARGUMENT")
    public void getUserPhotosReturnsInvalidArgumentWhenUserIdInvalid() {
        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.getUserPhotos(
                        PhotoPageRequest.newBuilder()
                                .setUserId("not-a-uuid")
                                .setPage(0)
                                .setSize(10)
                                .setIncludeTotal(true)
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    // ===== GetFeedPhotos (я + друзья) =====

    @Test
    @DisplayName("GetFeedPhotos: в выдаче есть мои фото и фото друга; сортировка по дате убыв.")
    @User(
            photos = {
                    @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "me1"),
                    @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "me2")
            },
            friends = 1,
            friendsWithPhotosEach = 2
    )
    public void getFeedPhotosContainsMineAndFriendWhenMixedAndSortedByCreationDesc(AppUser user) {
        final PhotosPageResponse feed = feedPhotosPage(user, 0, 20, true);

        final String myUserId = user.id().toString();
        final String friendUserId = friendId(user, 0).toString();

        assertAll("feed metadata",
                () -> assertTrue(feed.hasTotal(), "total should be present"),
                () -> assertEquals(4, feed.getTotal(), "total should be equals 4"),
                () -> assertEquals(4, feed.getContentCount(), "at least 4 photos expected"),
                () -> assertEquals(feed.getContentCount(), feed.getTotalElements(),
                        "totalElements should equal content length")
        );

        assertAll("ownership composition",
                () -> assertTrue(
                        feed.getContentList().stream().anyMatch(p -> myUserId.equals(p.getUserId())),
                        "should contain at least one of my photos"
                ),
                () -> assertTrue(
                        feed.getContentList().stream().anyMatch(p -> friendUserId.equals(p.getUserId())),
                        "should contain at least one friend's photo"
                )
        );

        final String firstId = feed.getContent(0).getPhotoId();
        final String secondId = feed.getContent(1).getPhotoId();
        assertNotEquals(firstId, secondId, "first and second should be different");
    }

    @Test
    @DisplayName("GetFeedPhotos: пагинация (size=1), корректные first/last")
    @User(
            photos = {
                    @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "me1"),
                    @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "me2")
            }, friends = 1, friendsWithPhotosEach = 2
    )
    public void getFeedPhotosSetsFirstLastFlagsWhenPageSizeIsOne(AppUser user) {
        final PhotosPageResponse page0 = feedPhotosPage(user, 0, 1, true);
        final PhotosPageResponse page1 = feedPhotosPage(user, 1, 1, true);

        assertAll("page 0 flags",
                () -> assertTrue(page0.hasTotal(), "total should be present"),
                () -> assertEquals(1, page0.getContentCount(), "content should be one"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertFalse(page0.getLast(), "last should be false")
        );

        assertAll("page 1 flags",
                () -> assertTrue(page1.hasTotal(), "total should be present"),
                () -> assertEquals(1, page1.getContentCount(), "content should be one"),
                () -> assertFalse(page1.getFirst(), "first should be false"),
                () -> assertFalse(page1.getLast(), "last should be false when more pages exist")
        );
    }


    @Test
    @DisplayName("GetFeedPhotos: пустой фид (content пуст, first=true,last=true, total=0)")
    @User
    public void getFeedPhotosReturnsEmptyPageWhenNoPhotos(AppUser user) {
        final PhotosPageResponse page0 = feedPhotosPage(user, 0, 10, true);

        assertAll("empty feed",
                () -> assertTrue(page0.hasTotal(), "total should be present"),
                () -> assertEquals(0, page0.getTotal(), "total should be 0"),
                () -> assertEquals(0, page0.getContentCount(), "content count should be 0"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertTrue(page0.getLast(), "last should be true")
        );
    }

    @Test
    @DisplayName("GetFeedPhotos: невалидный user_id → INVALID_ARGUMENT")
    public void getFeedPhotosReturnsInvalidArgumentWhenUserIdInvalid() {
        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.getFeedPhotos(
                        PhotoPageRequest.newBuilder()
                                .setUserId("not-a-uuid")
                                .setPage(0)
                                .setSize(10)
                                .setIncludeTotal(true)
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    // ===== Helpers =====

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

    private PhotosPageResponse feedPhotosPage(AppUser user, int page, int size, boolean includeTotal) {
        return photoBlockingStub.getFeedPhotos(
                PhotoPageRequest.newBuilder()
                        .setUserId(user.id().toString())
                        .setPage(page)
                        .setSize(size)
                        .setIncludeTotal(includeTotal)
                        .build()
        );
    }

}
