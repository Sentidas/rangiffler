package ru.sentidas.rangiffler.model;

import jakarta.annotation.Nonnull;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.grpc.UpdateUserRequest;
import ru.sentidas.rangiffler.grpc.UserResponse;

import java.util.UUID;

public record User(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,
        FriendshipStatus friendStatus,
        String countryCode
) {

    public static User fromEntity(UserEntity userEntity, FriendshipStatus friendStatus) {

        return new User(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getFirstname(),
                userEntity.getSurname(),
                null,  // avatar собирается отдельно в UserService.toUser(...)
                friendStatus,
                userEntity.getCountryCode()
        );
    }

    public static User fromEntity(UserEntity userEntity) {
        return fromEntity(userEntity, null);
    }

    public static @Nonnull User fromProto(@Nonnull UpdateUserRequest request) {
        String username = request.getUsername();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        return new User(
                null,
                username,
                (request.hasFirstname() && !request.getFirstname().isBlank()) ? request.getFirstname() : null,
                (request.hasSurname() && !request.getSurname().isBlank()) ? request.getSurname() : null,
                (request.hasAvatar() && !request.getAvatar().isBlank()) ? request.getAvatar() : null,
                null,
                (request.hasCountryCode() && !request.getCountryCode().isBlank()) ? request.getCountryCode() : null
        );
    }

    public void toProto(UserResponse.Builder b) {
        if (id != null) b.setId(id.toString());
        if (username != null) b.setUsername(username);
        if (firstname != null) b.setFirstname(firstname);
        if (surname != null) b.setSurname(surname);
        if (avatar != null) b.setAvatar(avatar);
        if (countryCode != null) b.setCountryCode(countryCode);
        if (friendStatus != null)
            b.setFriendStatus(ru.sentidas.rangiffler.grpc.FriendStatus.valueOf(friendStatus.name()));
    }
}
