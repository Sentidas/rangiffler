package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.data.entity.userdata.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;
import ru.sentidas.rangiffler.grpc.UpdateUserRequest;
import ru.sentidas.rangiffler.grpc.UserResponse;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Base64;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public record User(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,
        String avatarSmall,
        FriendshipStatus friendshipStatus,
        String countryCode,
        TestData testData
) {

    public static User fromEntity(UserEntity entity, FriendshipStatus friendshipStatus) {
        String avatarDataUrl = (entity.getAvatar() != null && entity.getAvatar().length > 0)
                ? "data:image/png;base64," + Base64.getEncoder().encodeToString(entity.getAvatar())
                : null;

        String smallDataUrl = (entity.getAvatarSmall() != null && entity.getAvatarSmall().length > 0)
                ? "data:image/png;base64," + Base64.getEncoder().encodeToString(entity.getAvatarSmall())
                : null;

        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getSurname(),
                avatarDataUrl,
                smallDataUrl,
                friendshipStatus,
                entity.getCountryCode(),
                new TestData(
                        null,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );
    }

    public User withPassword(String password) {
        return withTestData(
                new TestData(
                        password,
                        testData.photos(),
                        testData.friends(),
                        testData.incomeInvitations(),
                        testData.outcomeInvitations()
                )
        );
    }

    public User withTestData(TestData testData) {
        return new User(
                id,
                username,
                firstname,
                surname,
                avatar,
                avatarSmall,
                friendshipStatus,
                countryCode,
                testData
        );
    }

    public static User fromEntity(UserEntity userEntity) {
        return fromEntity(userEntity, null);
    }


    public void toProto(UserResponse.Builder b) {
        if (id != null) b.setId(id.toString());
        if (username != null) b.setUsername(username);
        if (firstname != null) b.setFirstname(firstname);
        if (surname != null) b.setSurname(surname);
        if (avatar != null) b.setAvatar(avatar);
        if (avatarSmall() != null) b.setAvatarSmall(avatarSmall);
        if (countryCode != null) b.setCountryCode(countryCode);
        if (friendshipStatus != null)
            b.setFriendStatus(ru.sentidas.rangiffler.grpc.FriendStatus.valueOf(friendshipStatus.name()));
    }
}
