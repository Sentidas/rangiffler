package ru.sentidas.rangiffler.model.ggl.input;

import java.util.UUID;

public record FriendshipInput(
        UUID user,
        FriendshipAction action
) {
}
