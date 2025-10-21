package ru.sentidas.rangiffler.test.grpc.photo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.PhotoResponse;
import ru.sentidas.rangiffler.grpc.UpdatePhotoRequest;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL2;

@GrpcTest
@DisplayName("Photo: updatePhoto")
public class UpdatePhotoTest extends BaseTest {

    @Test
    @User(photos = { @Photo(countryCode = "fr", description = "desc", src = DATA_URL) })
    @DisplayName("Обновление описания фото: значение меняется")
    public void updatePhotoChangesDescriptionWhenOwnerUpdates(AppUser user) {
        final String ownerId = user.id().toString();
        final AppPhoto photo = firstPhoto(user, user.id());

        final PhotoResponse updated = photoBlockingStub.updatePhoto(
                UpdatePhotoRequest.newBuilder()
                        .setPhotoId(photo.id().toString())
                        .setRequesterId(ownerId)
                        .setDescription("new desc")
                        .build()
        );

        assertEquals("new desc", updated.getDescription(), "description should be updated");
    }

    @Test
    @User(photos = { @Photo(countryCode = "fr", description = "desc", src = DATA_URL) })
    @DisplayName("Обновление страны фото: код меняется")
    public void updatePhotoChangesCountryCodeWhenOwnerUpdates(AppUser user) {
        final String ownerId = user.id().toString();
        final AppPhoto photo = firstPhoto(user, user.id());

        final PhotoResponse updated = photoBlockingStub.updatePhoto(
                UpdatePhotoRequest.newBuilder()
                        .setPhotoId(photo.id().toString())
                        .setRequesterId(ownerId)
                        .setCountryCode("ru")
                        .build()
        );

        assertEquals("ru", updated.getCountryCode(), "countryCode should be updated to 'ru'");
    }

    // ==== Негативные сценарии =====

    @Test
    @User(photo = 1, friends = 1)
    @DisplayName("Обновление не владельцем: PERMISSION_DENIED")
    public void updatePhotoReturnsPermissionDeniedWhenRequesterIsNotOwner(AppUser user) {
        final String friend = friendId(user, 0).toString();
        final String photoId = firstPhotoId(user, user.id()).toString();

        final StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                        .setPhotoId(photoId)
                        .setRequesterId(friend)
                        .build())
        );
        assertEquals(Status.PERMISSION_DENIED.getCode(), ex.getStatus().getCode(), "status should be PERMISSION_DENIED");
    }

    @Test
    @DisplayName("Неверный UUID photo_id: INVALID_ARGUMENT")
    public void updatePhotoReturnsInvalidArgumentWhenPhotoIdIsInvalid() {
        final StatusRuntimeException ex = Assertions.assertThrows(StatusRuntimeException.class, () ->
                photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                        .setPhotoId("not-a-uuid")
                        .setRequesterId("11111111-1111-1111-1111-111111111111")
                        .setDescription("x")
                        .build())
        );
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(), "status should be INVALID_ARGUMENT");
    }
}
