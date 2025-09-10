package ru.sentidas.rangiffler.model;

import java.util.UUID;

public record UserJson(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,
        FriendStatus friendStatus,
        String countryCode) {

}
