package ru.sentidas.rangiffler.model.ggl.input;

import ru.sentidas.rangiffler.model.FriendStatus;

import java.util.UUID;

public record UserGql(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,
        FriendStatus friendStatus,
        String countryCode,
        Country location
){
}
