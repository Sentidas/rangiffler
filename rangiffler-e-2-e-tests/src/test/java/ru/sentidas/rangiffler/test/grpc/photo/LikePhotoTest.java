package ru.sentidas.rangiffler.test.grpc.photo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.LikeRequest;
import ru.sentidas.rangiffler.grpc.PhotoPageRequest;
import ru.sentidas.rangiffler.grpc.PhotoResponse;
import ru.sentidas.rangiffler.grpc.PhotosPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.friendId;

@GrpcTest
@DisplayName("Photo: toggleLike")
public class LikePhotoTest extends BaseTest {

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("Друг лайкает фото: total=1 и друг есть в списке лайков и в листинге")
    public void toggleLikeIncrementsToOneAndListsFriendWhenFriendLikes(AppUser user) {
        final String targetPhotoId = firstPhotoId(userPhotosPage(user, 0, 20, true));
        final String friendUserId = friendId(user, 0).toString();

        PhotoResponse toggled = photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendUserId)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        assertAll("friend like",
                () -> assertEquals(1, toggled.getLikes().getTotal(), "total should be 1"),
                () -> assertTrue(likesContainUser(toggled, friendUserId), "friend should be in likes list")
        );

        PhotosPageResponse pageAfter = userPhotosPage(user, 0, 20, true);
        PhotoResponse firstPhotoAfter = pageAfter.getContent(0);

        assertEquals(targetPhotoId, firstPhotoAfter.getPhotoId(), "first item should be the target photo");
        assertAll("in feed response photo liked",
                () -> assertEquals(1, firstPhotoAfter.getLikes().getTotal(), "listing likes.total should be 1"),
                () -> assertTrue(likesContainUser(firstPhotoAfter, friendUserId), "listing should contain friend in likes")
        );
    }

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("Повторный toggleLike тем же пользователем снимает лайк: total=0")
    public void toggleLikeRemovesLikeAndTotalIsZeroWhenSameUserTogglesTwice(AppUser user) {
        final PhotosPageResponse pageBefore = userPhotosPage(user, 0, 20, true);

        final String targetPhotoId = firstPhotoId(pageBefore);
        final String friendUserId = friendId(user, 0).toString();

        // Первый toggle: поставить лайк от друга
        photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendUserId)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        // Второй toggle: снять лайк другом
        PhotoResponse toggledAgain = photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendUserId)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        assertAll("second toggle removes like",
                () -> assertEquals(0, toggledAgain.getLikes().getTotal(), "total should be 0"),
                () -> assertEquals(0, toggledAgain.getLikes().getLikesCount(), "likesCount should be 0")
        );
    }

    @Test
    @User(photo = 1, friends = 2)
    @DisplayName("Два разных пользователя лайкнули одно фото: total=2 и оба в списке")
    public void toggleLikeTotalsTwoWhenTwoDifferentUsersLikeSamePhoto(AppUser user) {
        final String targetPhotoId = firstPhotoId(userPhotosPage(user, 0, 20, true));

        final String friendA = friendId(user, 0).toString();
        final String friendB = friendId(user, 1).toString();

        // Первый пользователь ставит лайк
        PhotoResponse afterFirst = photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendA)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        assertAll("after first like",
                () -> assertEquals(1, afterFirst.getLikes().getTotal(), "total should be 1 after first like"),
                () -> assertTrue(likesContainUser(afterFirst, friendA), "friend A should be present after first like")
        );

        // Второй пользователь ставит лайк
        PhotoResponse afterSecond = photoBlockingStub.toggleLike(
                LikeRequest.newBuilder()
                        .setUserId(friendB)
                        .setPhotoId(targetPhotoId)
                        .build()
        );

        assertAll("after second like",
                () -> assertEquals(2, afterSecond.getLikes().getTotal(), "total should be 2 after second like"),
                () -> assertTrue(likesContainUser(afterSecond, friendA), "friend A should still be present"),
                () -> assertTrue(likesContainUser(afterSecond, friendB), "friend B should be present"),
                // дополнительная защита от дубликатов лайков одного и того же пользователя
                () -> assertEquals(
                        2,
                        afterSecond.getLikes().getLikesList().stream().map(ru.sentidas.rangiffler.grpc.Like::getUserId).distinct().count(),
                        "unique likers count should be 2"
                )
        );
    }

    // ==== Негативные сценарии =====

    @Test
    @User(photo = 1)
    @DisplayName("Владелец не может лайкнуть своё фото: PERMISSION_DENIED")
    public void toggleLikeReturnsPermissionDeniedWhenOwnerLikesOwnPhoto(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 20, true);
        final String targetPhotoId = firstPhotoId(before);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.toggleLike(
                        LikeRequest.newBuilder()
                                .setUserId(user.id().toString())
                                .setPhotoId(targetPhotoId)
                                .build()
                )
        );
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("Лайк на несуществующее фото: NOT_FOUND")
    public void toggleLikeReturnsNotFoundWhenPhotoDoesNotExist(AppUser user) {
        final String friendUserId = friendId(user, 0).toString();
        final String randomPhotoId = UUID.randomUUID().toString();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.toggleLike(
                        LikeRequest.newBuilder()
                                .setUserId(friendUserId)
                                .setPhotoId(randomPhotoId)
                                .build()
                )
        );
        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User(friends = 1)
    @DisplayName("Неверный UUID photo_id: INVALID_ARGUMENT")
    public void toggleLikeReturnsInvalidArgumentWhenPhotoIdInvalid(AppUser user) {
        final String friendUserId = friendId(user, 0).toString();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.toggleLike(
                        LikeRequest.newBuilder()
                                .setUserId(friendUserId)
                                .setPhotoId("not-a-uuid")
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    @Test
    @User(photo = 1)
    @DisplayName("Неверный UUID user_id: INVALID_ARGUMENT")
    public void toggleLikeReturnsInvalidArgumentWhenUserIdInvalid(AppUser user) {
        final PhotosPageResponse before = userPhotosPage(user, 0, 20, true);
        final String targetPhotoId = firstPhotoId(before);

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.toggleLike(
                        LikeRequest.newBuilder()
                                .setUserId("not-a-uuid")
                                .setPhotoId(targetPhotoId)
                                .build()
                )
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode());
    }

    // ===== Helpers ====

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

    private static boolean likesContainUser(PhotoResponse photoResponse, String userId) {
        for (ru.sentidas.rangiffler.grpc.Like like : photoResponse.getLikes().getLikesList()) {
            if (like.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

}
