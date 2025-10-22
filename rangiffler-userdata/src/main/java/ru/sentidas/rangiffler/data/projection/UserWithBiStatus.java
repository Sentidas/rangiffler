package ru.sentidas.rangiffler.data.projection;

import ru.sentidas.rangiffler.data.entity.FriendshipStatus;
import java.util.UUID;

/** Проекция пользователя с двумя направлениями статуса дружбы
 * (я→он и он→я) для страниц "все пользователи".
 * */
public record UserWithBiStatus(
    UUID id,
    String username,
    String firstname,
    String surname,
    byte[] photoSmall,
    FriendshipStatus outStatus,
    FriendshipStatus inStatus,
    String countryCode
) {
}
