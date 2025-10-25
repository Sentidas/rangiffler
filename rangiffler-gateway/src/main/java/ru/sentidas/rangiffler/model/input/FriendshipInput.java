package ru.sentidas.rangiffler.model.input;

import ru.sentidas.rangiffler.model.FriendshipAction;

import java.util.UUID;

public record FriendshipInput(
        UUID user,
        FriendshipAction action
) {
}
