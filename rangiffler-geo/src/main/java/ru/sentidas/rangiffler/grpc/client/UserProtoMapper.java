package ru.sentidas.rangiffler.grpc.client;

import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.model.FriendStatus;
import ru.sentidas.rangiffler.model.User;

import java.util.UUID;

public final class UserProtoMapper {

    private UserProtoMapper() {
    }

    public static User fromProto(UserResponse response) {
        return new User(
                UUID.fromString(response.getId()),
                response.getUsername(),
                response.hasFirstname() ? response.getFirstname() : null,
                response.hasSurname() ? response.getSurname() : null,
                response.hasAvatar() ? response.getAvatar() : null,
                response.hasFriendStatus()
                        ? FriendStatus.valueOf(response.getFriendStatus().name())
                        : null,
                response.getCountryCode(),
                null
        );
    }
}
