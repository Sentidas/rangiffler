package ru.sentidas.rangiffler.grpc.utils;

import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.model.User;

import java.util.UUID;

public final class UserProtoMapper {

    private UserProtoMapper() {
    }

    public static User fromProto(UserResponse response) {
        return new User(
                UUID.fromString(response.getId()),
                response.getUsername()
        );
    }


}
