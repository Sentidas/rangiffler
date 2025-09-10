package ru.sentidas.rangiffler.service.utils;

import ru.sentidas.rangiffler.model.FriendStatus;
import ru.sentidas.rangiffler.model.ggl.input.UserGql;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;

import java.util.List;
import java.util.UUID;

public final class UserProtoMapper {

    private UserProtoMapper() {
    }

    public static UserGql fromProto(UserResponse response) {
        return new UserGql(
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

    public static Slice<UserGql> fromProto(UsersPageResponse response) {
        List<UserGql> content = response.getContentList()
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
