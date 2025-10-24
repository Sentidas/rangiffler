package ru.sentidas.rangiffler.service.impl;

import com.google.protobuf.Empty;
import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.model.UsersPage;
import ru.sentidas.rangiffler.rest.AuthApi;
import ru.sentidas.rangiffler.rest.core.RestClient;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.service.UsersClient;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.UserData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.sentidas.rangiffler.utils.generation.GenerationDataUser.randomUsername;

@ParametersAreNonnullByDefault
public class UsersApiClient implements UsersClient {

    private static final Config CFG = Config.getInstance();
    private static final String defaultPassword = "12345";

    private final AuthApi authApi = new RestClient.EmtyRestClient(CFG.authUrl()).create(AuthApi.class);

    private final RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub stub;

    public UsersApiClient() {
        this.stub = GrpcChannels.userdataBlockingStub;
    }

    // ==== CREATE ====

    @Override
    @Step("Create user with username '{0}' using REST (auth) and gRPC")
    @Nonnull
    public AppUser createUser(String username, String password) {
        String currentUsername = username;

        for (int attempt = 1; attempt <= 3; attempt++) {
            final String attemptUsername = currentUsername;
            try {
                // 1) Регистрация в auth
                authApi.requestRegisterForm().execute();
                authApi.register(
                        attemptUsername,
                        password,
                        password,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
                ).execute();

                // 2) Чтение из userdata
                UsernameRequest request = UsernameRequest.newBuilder()
                        .setUsername(attemptUsername)
                        .build();
                UserResponse response = stub.currentUser(request);

                return new AppUser(
                        UUID.fromString(response.getId()),
                        attemptUsername,
                        response.hasFirstname() ? response.getFirstname() : null,
                        response.hasSurname() ? response.getSurname() : null,
                        response.hasAvatar() ? response.getAvatar() : null,
                        response.hasAvatarSmall() ? response.getAvatarSmall() : null,
                        null,
                        response.getCountryCode(),
                        new TestData(password)
                );
            } catch (IOException e) {
                if (attempt < 3) {
                    currentUsername = randomUsername();
                    continue;
                }
                throw new RuntimeException("Failed to register user using auth API: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Failed to create user after 3 attempts");
    }

    @Override
    @Step("Create full user with username '{0}' using REST (auth) and gRPC")
    @Nonnull
    public AppUser createFullUser(AppUser user) {
        String currentUsername = user.username();

        for (int attempt = 1; attempt <= 3; attempt++) {
            final String attemptUsername = currentUsername;
            try {
                // 1) Регистрация в auth
                authApi.requestRegisterForm().execute();
                authApi.register(
                        attemptUsername,
                        defaultPassword,
                        defaultPassword,
                        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
                ).execute();

                // 2) обновление (или создание и потом обновление) в userdata
                UpdateUserRequest.Builder updateUserRequest = UpdateUserRequest.newBuilder()
                        .setUsername(attemptUsername);

                if (user.firstname() != null && !user.firstname().isBlank())
                    updateUserRequest.setFirstname(user.firstname());
                if (user.surname() != null && !user.surname().isBlank()) updateUserRequest.setSurname(user.surname());
                if (user.avatar() != null && !user.avatar().isBlank()) updateUserRequest.setAvatar(user.avatar());
                if (user.countryCode() != null && !user.countryCode().isBlank()) {
                    updateUserRequest.setCountryCode(user.countryCode());
                }

                UserResponse userUpdateResponse = stub.updateUser(updateUserRequest.build());

                return new AppUser(
                        UUID.fromString(userUpdateResponse.getId()),
                        attemptUsername,
                        userUpdateResponse.hasFirstname() ? userUpdateResponse.getFirstname() : null,
                        userUpdateResponse.hasSurname() ? userUpdateResponse.getSurname() : null,
                        userUpdateResponse.hasAvatar() ? userUpdateResponse.getAvatar() : null,
                        userUpdateResponse.hasAvatarSmall() ? userUpdateResponse.getAvatarSmall() : null,
                        null,
                        userUpdateResponse.getCountryCode(),
                        new TestData(defaultPassword)
                );
            } catch (IOException e) {
                if (attempt < 3) {
                    currentUsername = GenerationDataUser.randomUsername();
                    continue;
                }
                throw new RuntimeException("Failed to register full user using auth API: " + e.getMessage(), e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create full user: " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Failed to create full user after 3 attempts");
    }

    // ==== UPDATE ====

    @Override
    @Step("Update user with username '{0}' using gRPC")
    @Nonnull
    public AppUser updateUser(AppUser user) {
        UpdateUserRequest.Builder updateUserRequest = UpdateUserRequest.newBuilder()
                .setUsername(user.username());

        if (user.firstname() != null && !user.firstname().isBlank()) updateUserRequest.setFirstname(user.firstname());
        if (user.surname() != null && !user.surname().isBlank()) updateUserRequest.setSurname(user.surname());
        if (user.avatar() != null && !user.avatar().isBlank()) updateUserRequest.setAvatar(user.avatar());
        if (user.countryCode() != null && !user.countryCode().isBlank()) {
            updateUserRequest.setCountryCode(user.countryCode());
        }

        UserResponse userUpdateResponse = stub.updateUser(updateUserRequest.build());

        return new AppUser(
                UUID.fromString(userUpdateResponse.getId()),
                user.username(),
                userUpdateResponse.hasFirstname() ? userUpdateResponse.getFirstname() : null,
                userUpdateResponse.hasSurname() ? userUpdateResponse.getSurname() : null,
                userUpdateResponse.hasAvatar() ? userUpdateResponse.getAvatar() : null,
                null,
                null,
                userUpdateResponse.getCountryCode(),
                new TestData(
                        defaultPassword
                )
        );
    }

    // ==== READ ====

    @Override
    @Step("Get user by username '{0}' using gRPC")
    public AppUser findUserByUsername(String username) {
        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();
        return toAppUser(stub.currentUser(request));
    }

    // ==== FRIENDS / INVITES ====

    @Override
    @Step("Remove friend: {0} -> {1} using gRPC")
    public void removeFriend(String username, UUID targetUserId) {
        FriendshipRequest req = FriendshipRequest.newBuilder()
                .setUsername(username)
                .setUser(targetUserId.toString())
                .build();

        stub.removeFriend(req);
    }

    @Override
    @Step("Create {1} income invitation(s) using gRPC (fullProfiles={2})")
    public List<AppUser> createIncomeInvitations(AppUser targetUser, int count, boolean fullProfiles) {
        final List<AppUser> inviters = new ArrayList<>();
        if (count <= 0) return inviters;

        for (int i = 0; i < count; i++) {
            AppUser inviter = fullProfiles
                    ? createFullUser(fullUserModel())
                    : createUser(randomUsername(), defaultPassword);

            // inviter -> targetUser
            FriendshipRequest req = FriendshipRequest.newBuilder()
                    .setUsername(inviter.username())
                    .setUser(targetUser.id().toString())
                    .build();
            stub.createFriendshipRequest(req);

            inviters.add(inviter);
        }
        return inviters;
    }

    @Override
    @Step("Create {1} outcome invitation(s) using gRPC (fullProfiles={2})")
    public List<AppUser> createOutcomeInvitations(AppUser targetUser, int count, boolean fullProfiles) {
        final List<AppUser> invitees = new ArrayList<>();
        if (count <= 0) return invitees;

        for (int i = 0; i < count; i++) {
            AppUser invitee = fullProfiles
                    ? createFullUser(fullUserModel())
                    : createUser(randomUsername(), defaultPassword);

            // targetUser -> invitee
            FriendshipRequest req = FriendshipRequest.newBuilder()
                    .setUsername(targetUser.username())
                    .setUser(invitee.id().toString())
                    .build();
            stub.createFriendshipRequest(req);

            invitees.add(invitee);
        }
        return invitees;
    }

    @Override
    @Step("Add {1} friend(s) using gRPC: create + invite + accept")
    public List<AppUser> addFriends(AppUser targetUser, int count) {
        final List<AppUser> friends = new ArrayList<>();
        if (count <= 0) return friends;

        for (int i = 0; i < count; i++) {
            AppUser friend = createUser(randomUsername(), defaultPassword);

            // 1) friend -> targetUser (инвайт)
            FriendshipRequest invite = FriendshipRequest.newBuilder()
                    .setUsername(friend.username())
                    .setUser(targetUser.id().toString())
                    .build();
            stub.createFriendshipRequest(invite);

            // 2) targetUser принимает (targetUser -> friend)
            FriendshipRequest accept = FriendshipRequest.newBuilder()
                    .setUsername(targetUser.username())
                    .setUser(friend.id().toString())
                    .build();
            stub.acceptFriendshipRequest(accept);

            friends.add(friend);
        }
        return friends;
    }

    @Override
    @Step("Add {1} FULL friend(s) using gRPC: createFull + invite + accept")
    public List<AppUser> addFullFriends(AppUser targetUser, int count) {
        List<AppUser> friends = new ArrayList<>();
        if (count <= 0) return friends;

        for (int i = 0; i < count; i++) {
            AppUser friend = createFullUser(fullUserModel());

            FriendshipRequest invite = FriendshipRequest.newBuilder()
                    .setUsername(friend.username())
                    .setUser(targetUser.id().toString())
                    .build();
            stub.createFriendshipRequest(invite);

            FriendshipRequest accept = FriendshipRequest.newBuilder()
                    .setUsername(targetUser.username())
                    .setUser(friend.id().toString())
                    .build();
            stub.acceptFriendshipRequest(accept);

            friends.add(friend);
        }
        return friends;
    }

    @Override
    public String getStorageMode() {
        UserStorageModeResponse storageMode = stub.getStorageMode(Empty.getDefaultInstance());
        return storageMode.getMode();
    }

    // ==== PAGES ====

    @Override
    @Step("Get all users page using gRPC: username='{0}', page={1}, size={2}, searchQuery='{3}'")
    public UsersPage allUsersPage(String username, int page, int size, @Nullable String searchQuery) {
        UserPageRequest.Builder b = UserPageRequest.newBuilder()
                .setUsername(username)
                .setPage(page)
                .setSize(size);
        if (searchQuery != null && !searchQuery.isBlank()) b.setSearchQuery(searchQuery);

        UsersPageResponse p = stub.allUsersPage(b.build());
        return toUsersPage(p);
    }

    @Override
    @Step("Get friends page using gRPC: username='{0}', page={1}, size={2}, searchQuery='{3}'")
    public UsersPage allFriendsPage(String username, int page, int size, @Nullable String searchQuery) {
        UserPageRequest.Builder b = UserPageRequest.newBuilder()
                .setUsername(username)
                .setPage(page)
                .setSize(size);
        if (searchQuery != null && !searchQuery.isBlank()) b.setSearchQuery(searchQuery);

        UsersPageResponse p = stub.allFriendsPage(b.build());
        return toUsersPage(p);
    }

    @Override
    @Step("Get income invitations page using gRPC: username='{0}', page={1}, size={2}, searchQuery='{3}'")
    public UsersPage incomeInvitationsPage(String username, int page, int size, @Nullable String searchQuery) {
        UserPageRequest.Builder b = UserPageRequest.newBuilder()
                .setUsername(username)
                .setPage(page)
                .setSize(size);
        if (searchQuery != null && !searchQuery.isBlank()) b.setSearchQuery(searchQuery);

        UsersPageResponse p = stub.incomeInvitations(b.build());
        return toUsersPage(p);
    }

    @Override
    @Step("Get outcome invitations page using gRPC: username='{0}', page={1}, size={2}, searchQuery='{3}'")
    public UsersPage outcomeInvitationsPage(String username, int page, int size, @Nullable String searchQuery) {
        UserPageRequest.Builder b = UserPageRequest.newBuilder()
                .setUsername(username)
                .setPage(page)
                .setSize(size);
        if (searchQuery != null && !searchQuery.isBlank()) b.setSearchQuery(searchQuery);

        UsersPageResponse p = stub.outcomeInvitations(b.build());
        return toUsersPage(p);
    }

    @Nonnull
    private static AppUser toAppUser(UserResponse response) {
        return new AppUser(
                response.getId().isBlank() ? null : UUID.fromString(response.getId()),
                response.getUsername(),
                response.hasFirstname() ? response.getFirstname() : null,
                response.hasSurname() ? response.getSurname() : null,
                response.hasAvatar() ? response.getAvatar() : null,
                response.hasAvatarSmall() ? response.getAvatarSmall() : null,
                null,
                response.getCountryCode(),
                new TestData(defaultPassword)
        );
    }

    @Nonnull
    private static UsersPage toUsersPage(UsersPageResponse p) {
        List<AppUser> content = p.getContentList().stream()
                .map(UsersApiClient::toAppUser)
                .collect(Collectors.toList());
        return new UsersPage(
                p.getTotalElements(),
                p.getTotalPages(),
                p.getFirst(),
                p.getLast(),
                p.getPage(),
                p.getSize(),
                content
        );
    }

    @Nonnull
    private AppUser fullUserModel() {
        UserData data = GenerationDataUser.randomUser();
        String avatar = GenerationDataUser.randomAvatarDataUrl();
        return new AppUser(
                null,
                randomUsername(),
                data.firstname(),
                data.surname(),
                avatar,
                null,
                null,
                data.countryCode(),
                new TestData(defaultPassword)
        );
    }
}
