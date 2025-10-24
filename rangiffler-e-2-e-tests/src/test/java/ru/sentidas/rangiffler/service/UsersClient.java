package ru.sentidas.rangiffler.service;

import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.UsersPage;

import java.util.List;
import java.util.UUID;

public interface UsersClient {

    AppUser findUserByUsername(String username);

    AppUser createUser(String username, String password);

    AppUser createFullUser(AppUser user);

    AppUser updateUser(AppUser user);

    List<AppUser> createIncomeInvitations(AppUser targetUser, int count, boolean fullProfile);

    List<AppUser> createOutcomeInvitations(AppUser targetUser, int count, boolean fullProfile);

    List<AppUser> addFriends(AppUser targetUser, int count);

    List<AppUser> addFullFriends(AppUser targetUser, int count);

    void removeFriend(String username, UUID targetUserId);

    UsersPage allUsersPage(String username, int page, int size, String searchQuery);

    UsersPage allFriendsPage(String username, int page, int size, String searchQuery);

    UsersPage incomeInvitationsPage(String username, int page, int size, String searchQuery);

    UsersPage outcomeInvitationsPage(String username, int page, int size, String searchQuery);

    String getStorageMode();
}


