package ru.sentidas.rangiffler.model;


import java.util.UUID;

public record User(
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
