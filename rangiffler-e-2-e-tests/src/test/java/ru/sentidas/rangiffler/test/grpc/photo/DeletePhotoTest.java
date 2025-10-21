package ru.sentidas.rangiffler.test.grpc.photo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.friendId;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL_PNG;

@GrpcTest
@DisplayName("Photo: deletePhoto")
public class DeletePhotoTest extends BaseTest {

    @Test
    @User(photo = 1)
    @DisplayName("Удаление владельцем: total уменьшился на 1, фото исчезло из выдачи")
    public void deleteOwnPhotoRemovesFromListingWhenOwnerRequests(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 5, true);
        assertTrue(before.getTotal() >= 1, "expected at least one photo before deletion");

        final String targetPhotoId = firstPhotoId(before);
        assertNotNull(targetPhotoId, "target photoId must not be null");

        photoBlockingStub.deletePhoto(
                DeletePhotoRequest.newBuilder()
                        .setRequesterId(user.id().toString())
                        .setId(targetPhotoId)
                        .build()
        );

        final PhotosPageResponse after = userPhotosPage(user, 0, 5, true);
        assertAll("owner deletes own photo",
                () -> assertEquals(before.getTotal() - 1, after.getTotal(), "total should decrease by one"),
                () -> assertFalse(containsPhotoId(after, targetPhotoId), "deleted photo should not be present")
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p1"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p2"),
            @Photo(countryCode = "fr", src = DATA_URL_PNG, description = "p3")
    })
    @DisplayName("Удаление второго фото: исчезает именно выбранное фото")
    public void deleteSpecificPhotoRemovesThatPhotoWhenMiddleItem(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 10, true);

        assertEquals(3, before.getContentCount(), "fixture should create three photos");

        final String idToDelete = before.getContent(1).getPhotoId(); // второе фото

        photoBlockingStub.deletePhoto(
                DeletePhotoRequest.newBuilder()
                        .setRequesterId(user.id().toString())
                        .setId(idToDelete)
                        .build()
        );

        final PhotosPageResponse after = userPhotosPage(user, 0, 10, true);
        assertAll("delete second photo",
                () -> assertEquals(2, after.getContentCount(), "content count should decrease to two"),
                () -> assertFalse(containsPhotoId(after, idToDelete), "deleted photo must not be present")
        );
    }

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("После удаления фото: попытка лайка даёт NOT_FOUND")
    public void likeAfterDelete_notFound(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 20, true);
        final String targetPhotoId = firstPhotoId(before);
        final String friendUserId = friendId(user, 0).toString();

        // Друг ставит лайк
        photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendUserId)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        // Владелец удаляет фото
        photoBlockingStub.deletePhoto(
                DeletePhotoRequest.newBuilder()
                        .setRequesterId(user.id().toString())
                        .setId(targetPhotoId)
                        .build()
        );

        // Попытка другом лайкнуть удалённое фото
        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.toggleLike(
                        LikeRequest.newBuilder()
                                .setUserId(friendUserId)
                                .setPhotoId(targetPhotoId)
                                .build()
                )
        );
        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());

        // Фото отсутствует
        final PhotosPageResponse after = userPhotosPage(user, 0, 20, true);
        assertFalse(containsPhotoId(after, targetPhotoId));
    }

    // ===== Негативные сценарии =====

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("Удаление чужого фото запрещено: PERMISSION_DENIED")
    public void deletePhotoReturnsPermissionDeniedWhenRequesterIsNotOwner(AppUser user) {
        final PhotosPageResponse page = userPhotosPage(user, 0, 20, true);
        final String targetPhotoId = firstPhotoId(page);

        final String friendRequester = friendId(user, 0).toString();

        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.deletePhoto(
                        DeletePhotoRequest.newBuilder()
                                .setRequesterId(friendRequester)
                                .setId(targetPhotoId)
                                .build()
                )
        );
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User
    @DisplayName("Удаление несуществующего фото: NOT_FOUND")
    public void deletePhotoReturnsNotFoundWhenPhotoDoesNotExist(AppUser user) {
        final String randomPhotoId = UUID.randomUUID().toString();

        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.deletePhoto(
                        DeletePhotoRequest.newBuilder()
                                .setRequesterId(user.id().toString())
                                .setId(randomPhotoId)
                                .build()
                )
        );
        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User
    @DisplayName("Неверный UUID photo_id: INVALID_ARGUMENT")
    public void deletePhotoReturnsInvalidArgumentWhenPhotoIdInvalid(AppUser user) {
        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.deletePhoto(
                        DeletePhotoRequest.newBuilder()
                                .setRequesterId(user.id().toString())
                                .setId("not-a-uuid")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User(photo = 1)
    @DisplayName("Неверный UUID requester_id: INVALID_ARGUMENT")
    public void deletePhotoReturnsInvalidArgumentWhenRequesterIdInvalid(AppUser user) {
        final PhotosPageResponse page = userPhotosPage(user, 0, 20, true);
        final String targetPhotoId = firstPhotoId(page);

        final StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.deletePhoto(
                        DeletePhotoRequest.newBuilder()
                                .setRequesterId("not-a-uuid")
                                .setId(targetPhotoId)
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

    private static String firstPhotoId(PhotosPageResponse response) {
        if (response.getContentCount() == 0) {
            return null;
        }
        return response.getContent(0).getPhotoId();
    }

    private static boolean containsPhotoId(PhotosPageResponse response, String photoId) {
        for (PhotoResponse photoResponse : response.getContentList()) {
            if (photoResponse.getPhotoId().equals(photoId)) {
                return true;
            }
        }
        return false;
    }
}
