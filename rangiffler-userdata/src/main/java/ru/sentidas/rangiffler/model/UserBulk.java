package ru.sentidas.rangiffler.model;

import jakarta.annotation.Nonnull;
import ru.sentidas.rangiffler.data.projection.UserWithStatus;
import ru.sentidas.rangiffler.grpc.UserResponse;

import java.util.UUID;

/** DTO для отдачи пользователя в списках
 * (avatarSmall-превью, вычисленный friendStatus, countryCode).
 * */
public record UserBulk(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,  // для совместимости с фронтом
        String avatarSmall,   // нативное поле для small
        FriendshipStatus friendStatus,
        String countryCode) {


    //Преобразует проекцию "друзья/входящие" в DTO с dataURL-миниатюрой и нужным friendStatus
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

    // Преобразует проекцию "исходящие" в DTO с dataURL-миниатюрой и friendStatus=INVITATION_SENT при PENDING
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

    //Преобразует би-направленную проекцию в DTO, сворачивая out/in статусы в единый friendStatus
    public static @Nonnull UserBulk fromUserBiProjection(
            @Nonnull ru.sentidas.rangiffler.data.projection.UserWithBiStatus p) {

        String small = (p.photoSmall() != null && p.photoSmall().length > 0)
                ? "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(p.photoSmall())
                : null;

        var out = p.outStatus(); // PENDING/ACCEPTED/null
        var in  = p.inStatus();  // PENDING/ACCEPTED/null

        FriendshipStatus fs;
        if (out == ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED
                || in == ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED) {
            fs = FriendshipStatus.FRIEND;
        } else if (out == ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING) {
            fs = FriendshipStatus.INVITATION_SENT;      // я отправил
        } else if (in == ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING) {
            fs = FriendshipStatus.INVITATION_RECEIVED;  // мне отправили
        } else {
            fs = null;
        }

        return new UserBulk(
                p.id(),
                p.username(),
                p.firstname(),
                p.surname(),
                small,  // avatar (совместимость)
                small,  // avatarSmall (нативное)
                fs,
                p.countryCode()
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
