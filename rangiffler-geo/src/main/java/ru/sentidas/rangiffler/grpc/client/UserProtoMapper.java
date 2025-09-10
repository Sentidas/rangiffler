package ru.sentidas.rangiffler.grpc.client;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;
import ru.sentidas.rangiffler.model.FriendStatus;
import ru.sentidas.rangiffler.model.User;

import java.util.List;
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

    public static Slice<User> fromProto(UsersPageResponse response) {
        List<User> content = response.getContentList()
                .stream()
                .map(UserProtoMapper::fromProto)
                .toList();

        boolean hasNext = !response.getLast();
        return new SliceImpl<>(content,
                PageRequest.of(response.getPage(), response.getSize()),
                hasNext
        );
    }

}
