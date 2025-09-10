package ru.sentidas.rangiffler.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sentidas.rangiffler.data.entity.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.grpc.UserResponse;

import java.util.Base64;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserBulk(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("username")
        String username,
        @JsonProperty("firstname")
        String firstname,
        @JsonProperty("surname")
        String surname,
        @JsonProperty("avatar")
        String avatar,
        @JsonProperty("friendshipStatus")
        FriendStatus friendStatus,
        String countryCode) {


    public String photo() {
        return null;
    }


    public String firstname() {
        return null;
    }


    public String surname() {
        return null;
    }


    public static UserBulk fromUserEntity(UserEntity userEntity, FriendStatus friendStatus) {

        String avatarDataUrl = null;
        if (userEntity.getAvatar() != null) {
            // Конвертируем byte[] в Data URL
            String base64Avatar = Base64.getEncoder().encodeToString(userEntity.getAvatar());
            avatarDataUrl = "data:image/png;base64," + base64Avatar;
        }

        return new UserBulk(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getFirstname(),
                userEntity.getSurname(),
                avatarDataUrl,
                friendStatus,
                userEntity.getCountryCode()
        );
    }


    public void toProto(UserResponse.Builder b) {
        if (id != null) b.setId(id.toString());
        if (username != null) b.setUsername(username);
        if (firstname != null) b.setFirstname(firstname);
        if (surname != null) b.setSurname(surname);

        if (avatar != null) b.setAvatar(avatar);
        if (friendStatus != null) {
            b.setFriendStatus(
                    ru.sentidas.rangiffler.grpc.FriendStatus.valueOf(friendStatus.name())
            );
        }
        if (countryCode != null) b.setCountryCode(countryCode);

    }

    private static String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }


//    public static @Nonnull UserJsonBulk fromFriendEntityProjection(@Nonnull UserWithStatus projection) {
//        return new UserJsonBulk(
//                projection.id(),
//                projection.username(),
//                projection.fullname(),
//                projection.currency(),
//                projection.photoSmall() != null && projection.photoSmall().length > 0 ? new String(projection.photoSmall(), StandardCharsets.UTF_8) : null,
//                projection.status() == guru.qa.niffler.data.FriendshipStatus.PENDING ? FriendshipStatus.INVITE_RECEIVED : FriendshipStatus.FRIEND
//        );
//    }
//
//    public static @Nonnull UserJsonBulk fromUserEntityProjection(@Nonnull UserWithStatus projection) {
//        return new UserJsonBulk(
//                projection.id(),
//                projection.username(),
//                projection.fullname(),
//                projection.currency(),
//                projection.photoSmall() != null && projection.photoSmall().length > 0 ? new String(projection.photoSmall(), StandardCharsets.UTF_8) : null,
//                projection.status() == guru.qa.niffler.data.FriendshipStatus.PENDING ? FriendshipStatus.INVITE_SENT : null
//        );
//    }
}
