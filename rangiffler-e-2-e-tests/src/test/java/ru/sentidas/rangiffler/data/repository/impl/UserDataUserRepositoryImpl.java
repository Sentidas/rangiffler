package ru.sentidas.rangiffler.data.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.userdata.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;
import ru.sentidas.rangiffler.data.repository.UserdataRepository;

import java.util.Optional;
import java.util.UUID;

import static ru.sentidas.rangiffler.data.jpa.EntityManagers.em;


public class UserDataUserRepositoryImpl implements UserdataRepository {

    private static final Config CFG = Config.getInstance();

    private final EntityManager entityManager = em(CFG.userdataJdbcUrl());

    static UserDataUserRepositoryImpl getInstance() {
        return new UserDataUserRepositoryImpl();
    }

    @Override
    public UserEntity create(UserEntity user) {
        entityManager.joinTransaction();
        entityManager.persist(user);
        return user;
    }

    @Override
    public Optional<UserEntity> findById(UUID id) {
        return Optional.ofNullable(
                entityManager.find(UserEntity.class, id));
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        try {
            return Optional.of(
                    entityManager.createQuery("SELECT u FROM UserEntity u WHERE u.username =: username", UserEntity.class)
                            .setParameter("username", username)
                            .getSingleResult()
            );
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public UserEntity update(UserEntity user) {
        entityManager.joinTransaction();
        entityManager.merge(user);
        return user;
    }

    @Override
    public void sendInvitation(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        requester.addFriends(FriendshipStatus.PENDING, addressee);
    }

    @Override
    public void addFriend(UserEntity requester, UserEntity addressee) {
        entityManager.joinTransaction();
        requester.addFriends(FriendshipStatus.ACCEPTED, addressee);
        addressee.addFriends(FriendshipStatus.ACCEPTED, requester);
    }

    @Override
    public void removeFriend(UserEntity requester, UserEntity addressee) {

    }

    @Override
    public void remove(UserEntity user) {
        entityManager.joinTransaction();
        UserEntity removableUser = entityManager.find(UserEntity.class, user.getId());
        if(removableUser !=null) {
            entityManager.remove(removableUser);
        }
    }
}
