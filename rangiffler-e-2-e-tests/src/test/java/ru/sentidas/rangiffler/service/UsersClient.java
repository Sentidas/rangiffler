package ru.sentidas.rangiffler.service;

import ru.sentidas.rangiffler.model.AppUser;

import java.util.List;
import java.util.Optional;

public interface UsersClient {

    AppUser createUser(String username, String password);

    AppUser createFullUser(AppUser user);

    AppUser updateUser(String username, AppUser user);

    void removeUser(String username);

    List<AppUser>  createIncomeInvitations(AppUser targetUser, int count);

    List<AppUser> createOutcomeInvitations(AppUser targetUser, int count);

    List<AppUser>  addFriends(AppUser targetUser, int count);

    Optional<AppUser> findUserByUsername(String username);


}
