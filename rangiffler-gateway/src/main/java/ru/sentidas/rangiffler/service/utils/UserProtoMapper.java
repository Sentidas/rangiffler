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

public final class UserProtoMapper {

    private UserProtoMapper() {}

    // === NEW: базовый URL и хелперы, как в фото ===
    private static String BASE_URL = "";

    /** Вызываем один раз при старте из GrpcUserdataClient (см. п.2). */
    public static void setBaseUrl(String baseUrl) {
        BASE_URL = (baseUrl == null) ? "" : baseUrl.replaceAll("/+$", "");
    }

    private static boolean isObjectKey(String s) {
        return s != null && !s.isBlank() && !s.startsWith("data:");
    }

    /** data:… пропускаем как есть; object key → абсолютный публичный URL /media/{key} */
    private static String toPublicUrlOrPassThrough(String srcOrKey) {
        if (srcOrKey == null || srcOrKey.isBlank()) return srcOrKey;
        if (!isObjectKey(srcOrKey)) return srcOrKey;
        String prefix = (BASE_URL != null && !BASE_URL.isBlank()) ? BASE_URL + "/media/" : "/media/";
        return prefix + srcOrKey;
    }
    // === /NEW ===

    public static UserGql fromProto(UserResponse response) {
        return new UserGql(
                UUID.fromString(response.getId()),
                response.getUsername(),
                response.hasFirstname() ? response.getFirstname() : null,
                response.hasSurname() ? response.getSurname() : null,
                // CHANGED: соберём URL для OBJECT-ключа, data: оставим как есть
                response.hasAvatar() ? toPublicUrlOrPassThrough(response.getAvatar()) : null,
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
                .map(UserProtoMapper::fromProto) // уже применяет URL-хелпер
                .toList();

        boolean hasNext = !response.getLast();
        return new SliceImpl<>(content,
                PageRequest.of(response.getPage(), response.getSize()),
                hasNext
        );
    }
}
