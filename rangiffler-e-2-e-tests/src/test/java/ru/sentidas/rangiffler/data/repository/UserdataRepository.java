package ru.sentidas.rangiffler.data.repository;

import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserdataRepository {

    UserEntity create(UserEntity user);

    Optional<UserEntity> findById(UUID id);

    Optional<UserEntity> findByUsername(String username);

    UserEntity update(UserEntity user);

    void sendInvitation(UserEntity requester, UserEntity addressee);

    void addFriend(UserEntity requester, UserEntity addressee);

    void removeFriend(UserEntity requester, UserEntity addressee);

    void remove(UserEntity user);

}
