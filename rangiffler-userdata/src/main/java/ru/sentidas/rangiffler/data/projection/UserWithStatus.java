package ru.sentidas.rangiffler.data.projection;


import ru.sentidas.rangiffler.data.entity.FriendshipStatus;

import java.util.UUID;

public record UserWithStatus(
    UUID id,
    String username,
    String firstname,
    String surname,
    byte[] photoSmall,
    FriendshipStatus status,
    String countryCode
) {
}
