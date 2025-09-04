package guru.qa.rangiffler.model.input;

import guru.qa.rangiffler.model.FriendshipAction;

import java.util.UUID;

public record FriendshipInput(
        UUID user,
        FriendshipAction action
) {
}
