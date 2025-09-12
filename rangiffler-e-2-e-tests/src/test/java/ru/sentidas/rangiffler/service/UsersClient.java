package ru.sentidas.rangiffler.service;

import ru.sentidas.rangiffler.model.User;

import java.util.List;
import java.util.Optional;

public interface UsersClient {

    User createUser(String username, String password);

    User createFullUser(User user);

    User updateUser(String username, User user);

    void removeUser(String username);

    List<User>  createIncomeInvitations(User targetUser, int count);

    List<User> createOutcomeInvitations(User targetUser, int count);

    List<User>  addFriends(User targetUser, int count);

    Optional<User> findUserByUsername(String username);


}
