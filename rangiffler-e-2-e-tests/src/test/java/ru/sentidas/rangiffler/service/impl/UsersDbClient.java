package ru.sentidas.rangiffler.service.impl;

import io.qameta.allure.Step;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.auth.AuthUserEntity;
import ru.sentidas.rangiffler.data.entity.auth.Authority;
import ru.sentidas.rangiffler.data.entity.auth.AuthorityEntity;
import ru.sentidas.rangiffler.data.entity.userdata.StorageType;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;
import ru.sentidas.rangiffler.data.repository.AuthUserRepository;
import ru.sentidas.rangiffler.data.repository.UserdataRepository;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.model.UsersPage;
import ru.sentidas.rangiffler.service.UsersClient;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.UserData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static ru.sentidas.rangiffler.AvatarSmall.fromBytes;
import static ru.sentidas.rangiffler.DataUrl.parse;
import static ru.sentidas.rangiffler.model.AppUser.fromEntity;
import static ru.sentidas.rangiffler.utils.generation.GenerationDataUser.randomUsername;

@ParametersAreNonnullByDefault
public class UsersDbClient implements UsersClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final String defaultPassword = "12345";

    private final AuthUserRepository authUserRepositoryImpl = AuthUserRepository.getInstance();
    private final UserdataRepository userdataUserRepository = UserdataRepository.getInstance();

    // Клиент api_grpc, для работы с изображениями и minio в режиме OBJECT через сервис Userdata
    // и получения типа режима OBJECT или BLOB из настроек Userdata
    private final UsersApiClient usersApiClient = new UsersApiClient();

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );

    private final String storageMode;

    public UsersDbClient() {
        this.storageMode = usersApiClient.getStorageMode();
    }

    @Override
    @Step("Get storage mode from userdata service (cached)")
    public String getStorageMode() {
        return storageMode;
    }

    @Override
    @Step("Create user with username '{0}' using SQL INSERT")
    public AppUser createUser(String username, String password) {
        String currentUsername = username;

        for (int attempt = 1; attempt <= 3; attempt++) {
            final String attemptUsername = currentUsername;
            try {
                return requireNonNull(
                        xaTxTemplate.execute(() ->
                                fromEntity(createNewUser(attemptUsername, password), null)
                                        .withPassword(password)
                        )
                );
            } catch (Exception e) {
                if (isDuplicateUsername(e) && attempt < 3) {
                    currentUsername = randomUsername();
                    continue;
                }
                throw new RuntimeException("Failed to create user: " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Failed to create user after 3 attempts");
    }


    @Override
    @Step("Create FULL user using SQL INSERT (BLOB) or using gRPC for avatar (OBJECT)")
    public AppUser createFullUser(AppUser fullUser) {
        String currentUsername = fullUser.username();

        for (int attempt = 1; attempt <= 3; attempt++) {
            final String attemptUsername = currentUsername;
            try {
                UserEntity created = createNewFullUser(fullUser, attemptUsername);
                return fromEntity(created, null).withPassword(defaultPassword);
            } catch (Exception e) {
                if (isDuplicateUsername(e) && attempt < 3) {
                    currentUsername = randomUsername();
                    continue;
                }
                throw new RuntimeException("Failed to create full user: " + e.getMessage(), e);
            }
        }
        throw new IllegalStateException("Failed to create full user after 3 attempts");
    }


    @Override
    @Step("Update user '{0}' using SQL or using gRPC for avatar (OBJECT)")
    public AppUser updateUser(AppUser updatedUser) {
        final String username = updatedUser.username();
        final String avatar = updatedUser.avatar();
        final String avatarSmall = updatedUser.avatarSmall();

        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = authUserRepositoryImpl.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in auth: " + username));

            if (updatedUser.username() != null) {
                authUser.setUsername(updatedUser.username());
            }
            authUserRepositoryImpl.update(authUser);

            UserEntity user = userdataUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in userData: " + username));

            if (updatedUser.username() != null) user.setUsername(updatedUser.username());
            if (updatedUser.firstname() != null) user.setFirstname(updatedUser.firstname());
            if (updatedUser.surname() != null) user.setSurname(updatedUser.surname());
            if (updatedUser.countryCode() != null) user.setCountryCode(updatedUser.countryCode());

            if ("OBJECT".equalsIgnoreCase(storageMode)) {
                // при OBJECT, если пришёл dataURL (реальное изменение изображения) — делегация в API-клиент,
                // затем перечитываем пользователя из БД (там уже будет URL)
                boolean hasDataUrlChange = isDataUrl(avatar);
                if (hasDataUrlChange) {
                    usersApiClient.updateUser(updatedUser);

                    UserEntity fromDb = userdataUserRepository.findByUsername(user.getUsername())
                            .orElseThrow(() -> new IllegalStateException("User not found after gRPC update: " + user.getUsername()));
                    return fromEntity(fromDb, null);
                } else {
                    // URL или null → изображение не менялось, просто сохраняем прочие поля
                    userdataUserRepository.update(user);
                    return fromEntity(user, null);
                }
            } else {
                // === BLOB ===
                if (isDataUrl(avatar)) {
                    // 1) Парсим dataURL общей библиотекой rangiffler-media-common
                    DataUrl dataUrl = parse(avatar);
                    byte[] original = dataUrl.bytes();
                    String mime = dataUrl.mime();

                    // 2) Генерируем small 100x100 общей библиотекой rangiffler-media-common
                    byte[] small = fromBytes(original, 100);

                    user.setAvatar(original);
                    user.setAvatarSmall(small);
                    user.setMime(mime);
                }

                if (isDataUrl(avatarSmall)) {
                    DataUrl dataUrl = parse(avatarSmall);
                    user.setAvatarSmall(dataUrl.bytes());
                }

                userdataUserRepository.update(user);
                return fromEntity(user, null);
            }
        });
    }

    @Override
    @Step("Get user '{0}' using SQL")
    public AppUser findUserByUsername(String username) {
        return xaTxTemplate.execute(() ->
                userdataUserRepository.findByUsername(username)
                        .map(entity -> fromEntity(entity, null))
                        .orElseThrow(() -> new IllegalStateException("User not found: " + username))
        );
    }

    @Override
    @Step("Create {1} income invitation(s) using SQL (fullProfiles={2})")
    public List<AppUser> createIncomeInvitations(AppUser targetUser, int count, boolean fullProfiles) {
        final List<AppUser> incomeInvitations = new ArrayList<>();
        if (count <= 0) return incomeInvitations;

        for (int i = 0; i < count; i++) {
            AppUser invitingUser = fullProfiles
                    ? createFullUser(randomFullUserModel())
                    : createUser(randomUsername(), defaultPassword);

            xaTxTemplate.execute(() -> {
                UserEntity targetEntity = getUserEntity(targetUser);
                UserEntity invitingEntity = getUserEntity(invitingUser);
                userdataUserRepository.sendInvitation(invitingEntity, targetEntity);
                return null;
            });

            incomeInvitations.add(invitingUser);
        }
        return incomeInvitations;
    }

    @Override
    @Step("Create {1} outcome invitation(s) using SQL (fullProfiles={2})")
    public List<AppUser> createOutcomeInvitations(AppUser targetUser, int count, boolean fullProfiles) {
        final List<AppUser> outcomeInvitations = new ArrayList<>();
        if (count <= 0) return outcomeInvitations;

        for (int i = 0; i < count; i++) {
            AppUser invitedUser = fullProfiles
                    ? createFullUser(randomFullUserModel())
                    : createUser(randomUsername(), defaultPassword);

            xaTxTemplate.execute(() -> {
                UserEntity targetEntity = getUserEntity(targetUser);
                UserEntity invitedEntity = getUserEntity(invitedUser);
                userdataUserRepository.sendInvitation(targetEntity, invitedEntity);
                return null;
            });

            outcomeInvitations.add(invitedUser);
        }
        return outcomeInvitations;
    }

    @Override
    @Step("Add {1} friends for user using SQL INSERT")
    public List<AppUser> addFriends(AppUser targetUser, int count) {
        final List<AppUser> friends = new ArrayList<>();

        if (count <= 0) {
            return friends;
        }

        for (int i = 0; i < count; i++) {
            AppUser friend = xaTxTemplate.execute(() -> {
                UserEntity targetEntity = userdataUserRepository.findByUsername(targetUser.username())
                        .orElseThrow(() -> new IllegalStateException("Target not found: " + targetUser.username()));

                String friendUsername = randomUsername();
                AuthUserEntity auth = authUserEntity(friendUsername, defaultPassword);
                authUserRepositoryImpl.create(auth);

                UserEntity friendEntity = userdataUserRepository.create(userEntity(friendUsername));

                userdataUserRepository.addFriend(targetEntity, friendEntity);

                return fromEntity(friendEntity, null).withPassword(defaultPassword);
            });

            friends.add(friend);
        }
        return friends;
    }

    @Override
    @Step("Add {1} full friends for user using SQL INSERT")
    public List<AppUser> addFullFriends(AppUser targetUser, int count) {
        final List<AppUser> friends = new ArrayList<>();
        if (count <= 0) return friends;

        for (int i = 0; i < count; i++) {
            AppUser friendModel = randomFullUserModel();
            AppUser friend = createFullUser(friendModel);

            xaTxTemplate.execute(() -> {
                UserEntity targetEntity = getUserEntity(targetUser);
                UserEntity friendEntity = getUserEntity(friend);
                userdataUserRepository.addFriend(targetEntity, friendEntity);
                return null;
            });

            friends.add(friend.withPassword(defaultPassword));
        }
        return friends;
    }

    @Override
    @Step("Remove friend relation between '{0}' and '{1}' using SQL")
    public void removeFriend(String username, UUID targetUserId) {
        xaTxTemplate.execute(() -> {
            UserEntity requester = userdataUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Requester not found: " + username));
            UserEntity addressee = userdataUserRepository.findById(targetUserId)
                    .orElseThrow(() -> new IllegalStateException("Addressee not found: " + targetUserId));
            userdataUserRepository.removeFriend(requester, addressee);
            return null;
        });
    }

    @Override
    public UsersPage allUsersPage(String username, int page, int size, String searchQuery) {
        throw new UnsupportedOperationException("DB client does not provide paging; use API client");
    }

    @Override
    public UsersPage allFriendsPage(String username, int page, int size, String searchQuery) {
        throw new UnsupportedOperationException("DB client does not provide paging; use API client");
    }

    @Override
    public UsersPage incomeInvitationsPage(String username, int page, int size, String searchQuery) {
        throw new UnsupportedOperationException("DB client does not provide paging; use API client");
    }

    @Override
    public UsersPage outcomeInvitationsPage(String username, int page, int size, String searchQuery) {
        throw new UnsupportedOperationException("DB client does not provide paging; use API client");
    }


    @Nonnull
    private UserEntity getUserEntity(AppUser targetUser) {
        UserEntity targetEntity = userdataUserRepository.findByUsername(
                targetUser.username()
        ).orElseThrow();
        return targetEntity;
    }


    @Nonnull
    private UserEntity userEntity(String username) {
        UserEntity ue = new UserEntity();
        ue.setUsername(username);
        ue.setCountryCode("ru");
        ue.setStorage(StorageType.valueOf(storageMode));
        return ue;
    }

    @Nonnull
    private UserEntity createNewUser(String username, String password) {
        AuthUserEntity authUser = authUserEntity(username, password);
        authUserRepositoryImpl.create(authUser);
        return userdataUserRepository.create(userEntity(username));
    }


    private boolean isDuplicateUsername(Exception e) {
        Throwable t = e;
        while (t != null) {
            String message = t.getMessage();
            if (message != null && message.contains("Duplicate entry")
                    && message.contains("user.username")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    private UserEntity createNewFullUser(AppUser user, String username) {
        return xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = authUserEntity(username, user.testData().password());
            authUserRepositoryImpl.create(authUser);

            UserEntity ue = UserEntity.from(user);
            ue.setUsername(username);
            if (ue.getCountryCode() == null || ue.getCountryCode().isBlank()) {
                ue.setCountryCode(
                        user.countryCode() != null && !user.countryCode().isBlank()
                                ? user.countryCode()
                                : "ru");
            }

            if ("OBJECT".equalsIgnoreCase(storageMode)) {
                usersApiClient.updateUser(user);

                UserEntity fromDb = userdataUserRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("User not found after gRPC update: " + username));
                return fromDb;

            } else {
                // === BLOB ===
                ue.setStorage(StorageType.BLOB);
                ue = userdataUserRepository.create(ue);

                if (isDataUrl(user.avatar())) {
                    // 1) Парсим dataURL
                    DataUrl dataUrl = parse(user.avatar());
                    byte[] original = dataUrl.bytes();
                    String mime = dataUrl.mime();

                    // 2) Генерируем small 100x100 через общий механизм
                    byte[] small = fromBytes(original, 100);

                    // 3) Пишем в БД три параметра
                    ue.setAvatar(original);
                    ue.setAvatarSmall(small);
                    ue.setMime(mime);
                }

                userdataUserRepository.update(ue);
                return userdataUserRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException("User not found after create+enrich: " + username));
            }
        });
    }

    @Nonnull
    private AuthUserEntity authUserEntity(String username, String password) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(username);
        authUser.setPassword(pe.encode(password));
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);
        authUser.setAuthorities(
                Arrays.stream(Authority.values()).map(
                        e -> {
                            AuthorityEntity ae = new AuthorityEntity();
                            ae.setUser(authUser);
                            ae.setAuthority(e);
                            return ae;
                        }
                ).toList()
        );
        return authUser;
    }


    private AppUser randomFullUserModel() {
        final UserData userData = GenerationDataUser.randomUser();
        final String avatar = GenerationDataUser.randomAvatarDataUrl();

        return new AppUser(
                null,
                randomUsername(),
                userData.firstname(),
                userData.surname(),
                avatar,
                null,
                null,
                userData.countryCode(),
                new TestData(
                        defaultPassword,
                        new ArrayList<>(), // photos
                        new ArrayList<>(), // friends
                        0,
                        new ArrayList<>(), // incomeInvitations
                        new ArrayList<>()  // outcomeInvitations
                )
        );
    }

    private static boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:image");
    }
}

