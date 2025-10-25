package ru.sentidas.rangiffler;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventType {
    USER_REGISTERED,

    USER_LOGIN_SUCCEEDED,
    USER_LOGIN_FAILED,

    PHOTO_ADDED,
    PHOTO_UPDATED,
    PHOTO_DELETED,

    LIKE_ADDED,
    LIKE_REMOVED,

    FRIEND_INVITE_SENT,
    FRIEND_INVITE_ACCEPTED,
    FRIEND_INVITE_DECLINED,
    FRIEND_REMOVED,

    @JsonEnumDefaultValue
    UNKNOWN
}
