package ru.sentidas.rangiffler.service.utils;

import ru.sentidas.rangiffler.model.FriendStatus;
import ru.sentidas.rangiffler.model.UserGql;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;

import java.util.List;
import java.util.UUID;

public final class ProtoMapper {

    private ProtoMapper() {}

    public static String normalizeMediaPath(String value, String baseUrl) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.startsWith("data:")
                || value.startsWith("http")
                || value.startsWith("/")) {
            return value;
        }
        // objectKey  → добавить единый префикс
        String path = "/media/" + value;
        return (baseUrl == null || baseUrl.isBlank()) ? path : baseUrl + path;
    }

    public static UserGql fromProto(UserResponse response, String baseUrl) {
        String avatarPath = response.hasAvatar()
                ? normalizeMediaPath(response.getAvatar(), baseUrl)
                : null;

        FriendStatus friendStatus = response.hasFriendStatus()
                ? FriendStatus.valueOf(response.getFriendStatus().name())
                : null;

        return new UserGql(
                UUID.fromString(response.getId()),
                response.getUsername(),
                response.hasFirstname() ? response.getFirstname() : null,
                response.hasSurname() ? response.getSurname() : null,
                avatarPath,
                friendStatus,
                response.getCountryCode(),
                null
        );
    }

    public static Slice<UserGql> fromProto(UsersPageResponse response, String baseUrl) {
        List<UserGql> content = response.getContentList()
                .stream()
                .map(r -> fromProto(r, baseUrl))
                .toList();

        boolean hasNext = !response.getLast();
        return new SliceImpl<>(content,
                PageRequest.of(response.getPage(), response.getSize()),
                hasNext
        );
    }

    public static UserGql fromProto(UserResponse response) {
        String avatarPath = response.hasAvatar() ? response.getAvatar() : null;

        FriendStatus friendStatus = response.hasFriendStatus()
                ? FriendStatus.valueOf(response.getFriendStatus().name())
                : null;

        return new UserGql(
                UUID.fromString(response.getId()),
                response.getUsername(),
                response.hasFirstname() ? response.getFirstname() : null,
                response.hasSurname() ? response.getSurname() : null,
                avatarPath,
                friendStatus,
                response.getCountryCode(),
                null
        );
    }

    public static Slice<UserGql> fromProto(UsersPageResponse response) {
        List<UserGql> content = response.getContentList()
                .stream()
                .map(ProtoMapper::fromProto)
                .toList();

        boolean hasNext = !response.getLast();
        return new SliceImpl<>(content,
                PageRequest.of(response.getPage(), response.getSize()),
                hasNext
        );
    }
}
