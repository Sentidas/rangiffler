package ru.sentidas.rangiffler.model;

import java.util.Date;
import java.util.UUID;

public record Like(
        UUID user,
        String username,
        Date creationDate

) {

}
