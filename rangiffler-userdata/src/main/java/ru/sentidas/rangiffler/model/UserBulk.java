package ru.sentidas.rangiffler.model;

import jakarta.annotation.Nonnull;
import ru.sentidas.rangiffler.data.projection.UserWithStatus;
import ru.sentidas.rangiffler.grpc.UserResponse;

import java.util.UUID;

public record UserBulk(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,  // для совместимости с фронтом
        String avatarSmall,   // нативное поле для small
        FriendshipStatus friendStatus,
        String countryCode) {


    public static @Nonnull UserBulk fromFriendEntityProjection(@Nonnull UserWithStatus projection) {
        String small = (projection.photoSmall() != null && projection.photoSmall().length > 0)
                ? "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(projection.photoSmall())
                : null;

        return new UserBulk(
                projection.id(),
                projection.username(),
                projection.firstname(),
                projection.surname(),
                small,              // совместимость
                small,              // нативное поле
                projection.status() == ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
                        ? FriendshipStatus.INVITATION_RECEIVED
                        : FriendshipStatus.FRIEND,
                projection.countryCode()
        );
    }

    public static @Nonnull UserBulk fromUserEntityProjection(@Nonnull UserWithStatus projection) {
        String small = (projection.photoSmall() != null && projection.photoSmall().length > 0)
                ? "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(projection.photoSmall())
                : null;

        return new UserBulk(
                projection.id(),
                projection.username(),
                projection.firstname(),
                projection.surname(),
                small,
                small,
                projection.status() == ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
                        ? FriendshipStatus.INVITATION_SENT
                        : null,
                projection.countryCode()
        );
    }

    public void toProto(UserResponse.Builder b) {
        if (id != null) b.setId(id.toString());
        if (username != null) b.setUsername(username);
        if (firstname != null) b.setFirstname(firstname);
        if (surname != null) b.setSurname(surname);
        if (avatar != null) b.setAvatar(avatar);
        if (avatarSmall != null) b.setAvatarSmall(avatarSmall);
        if (friendStatus != null) {
            b.setFriendStatus(
                    ru.sentidas.rangiffler.grpc.FriendStatus.valueOf(friendStatus.name())
            );
        }
        if (countryCode != null) b.setCountryCode(countryCode);
    }
}
