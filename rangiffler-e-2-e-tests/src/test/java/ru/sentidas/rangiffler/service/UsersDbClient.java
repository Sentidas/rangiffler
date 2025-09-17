package ru.sentidas.rangiffler.service;

import io.qameta.allure.Step;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.entity.auth.AuthUserEntity;
import ru.sentidas.rangiffler.data.entity.auth.Authority;
import ru.sentidas.rangiffler.data.entity.auth.AuthorityEntity;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;
import ru.sentidas.rangiffler.data.repository.UserdataRepository;
import ru.sentidas.rangiffler.data.repository.impl.AuthUserRepository;
import ru.sentidas.rangiffler.data.repository.impl.UserDataUserRepository;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.utils.generator.RandomDataUtils;
import ru.sentidas.rangiffler.utils.generator.UserData;
import ru.sentidas.rangiffler.utils.generator.UserDataGenerator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class UsersDbClient implements ru.sentidas.rangiffler.service.UsersClient {

    private static final Config CFG = Config.getInstance();
    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final String defaultPassword = "12345";

    private final ru.sentidas.rangiffler.data.repository.AuthUserRepository authUserRepository = new AuthUserRepository();
    private final UserdataRepository userdataUserRepository = new UserDataUserRepository();

    private final XaTransactionTemplate xaTxTemplate = new XaTransactionTemplate(
            CFG.authJdbcUrl(),
            CFG.userdataJdbcUrl()
    );


    @Override
    @Step("Create userId with username '{0}' using SQL INSERT")
    @Nonnull
    public AppUser createUser(String username, String password) {
        return requireNonNull(
                xaTxTemplate.execute(
                        () -> AppUser.fromEntity(
                                createNewUser(username, password),
                                null
                        ).withPassword(password)
                )
        );
    }


    @Override
    @Step("Create userId with username '{0}' using SQL INSERT")
    @Nonnull
    public AppUser createFullUser(AppUser user) {
        return requireNonNull(
                xaTxTemplate.execute(
                        () -> AppUser.fromEntity(
                                createNewUser(user),
                                null
                        ).withPassword(defaultPassword)
                )
        );
    }

    @Step("Delete userId '{0}' using SQL")
    public void removeUser(String username) {
        xaTxTemplate.execute(() -> {
            AuthUserEntity authUser = authUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in auth: " + username));

            authUserRepository.remove(authUser);

            UserEntity user = userdataUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in userData: " + username));
            userdataUserRepository.remove(user);

            return null;
        });
    }

    @Step("Update userId '{0}' using SQL")
    public AppUser updateUser(String username, AppUser updatedUser) {
        return xaTxTemplate.execute(() -> {

            //  String username  = Optional.of(authUserRepository.findByUsername(updatedUser.username()));

            AuthUserEntity authUser = authUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in auth: " + username));

            if (updatedUser.username() != null) {
                authUser.setUsername(updatedUser.username());
            }

            authUserRepository.update(authUser);

            UserEntity user = userdataUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("User not found in userData: " + username));

            if (updatedUser.username() != null) {
                user.setUsername(updatedUser.username());
            }

            if (updatedUser.firstname() != null) {
                user.setFirstname(updatedUser.firstname());
            }

            if (updatedUser.surname() != null) {
                user.setSurname(updatedUser.surname());
            }

            if (updatedUser.countryCode() != null) {
                user.setCountryCode(updatedUser.countryCode());
                  }

            // avatar (big) + автогенерация small при его отсутствии
            if (isDataUrl(updatedUser.avatar())) {
                String b64 = updatedUser.avatar().substring(updatedUser.avatar().indexOf(',') + 1);
                user.setAvatar(Base64.getDecoder().decode(b64));

                if (updatedUser.avatarSmall() == null) {
                    byte[] smallBytes = new SmallAvatar(75, 100, "png", updatedUser.avatar()).bytes();
                    user.setAvatarSmall(toRawBytesIfDataUrlBytes(smallBytes));
                }
            }

// avatarSmall, если фронт прислал отдельно
            if (isDataUrl(updatedUser.avatarSmall())) {
                String b64s = updatedUser.avatarSmall().substring(updatedUser.avatarSmall().indexOf(',') + 1);
                user.setAvatarSmall(Base64.getDecoder().decode(b64s));
            }

            userdataUserRepository.update(user);

            return AppUser.fromEntity(user, null);
        });
    }


    @Step("Get userId '{0}' using SQL")
    public Optional<AppUser> findUserByUsername(String username) {
        return xaTxTemplate.execute(() ->
                userdataUserRepository.findByUsername(username)
                        .map(entity -> AppUser.fromEntity(entity, null))
        );
    }


//    @Step("Create {1} income invitation using SQL")
//    public List<User> createIncomeInvitations(User targetUser, int count) {
//        List<User> incomeInvitations = new ArrayList<>();
//
//        if (count > 0) {
//            UserEntity targetEntity = userdataUserRepository.findByUsername(
//                    targetUser.username()
//            ).orElseThrow();
//
//            for (int i = 0; i < count; i++) {
//                User addressee = xaTxTemplate.execute(() -> {
//
//                    String username = RandomDataUtils.randomUsername();
//
//                    AuthUserEntity authUser = authUserEntity(username, "12345");
//                    authUserRepository.create(authUser);
//                    UserEntity fromUser = userdataUserRepository.create(userEntity(username));
//
//                    userdataUserRepository.sendInvitation(fromUser, targetEntity);
//                    return User.fromEntity(fromUser, null);
//                });
//                incomeInvitations.add(addressee);
//            }
//        }
//        return incomeInvitations;
//    }

    @Step("Create {1} income invitation using SQL")
    public List<AppUser> createIncomeInvitations(AppUser targetUser, int count) {
        List<AppUser> incomeInvitations = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findByUsername(
                    targetUser.username()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                AppUser inviter = xaTxTemplate.execute(() -> {

                    String username = RandomDataUtils.randomUsername();
                    AppUser full = randomFullUserModel();
                    UserEntity fromUser = createNewUser(full);

                    userdataUserRepository.sendInvitation(fromUser, targetEntity);
                    return AppUser.fromEntity(fromUser, null).withPassword(defaultPassword);
                });
                incomeInvitations.add(inviter);
            }
        }
        return incomeInvitations;
    }

//    @Step("Create {1} outcome invitation using SQL")
//    public List<User> createOutcomeInvitations(User targetUser, int count) {
//        List<User> outcomeInvitations = new ArrayList<>();
//
//        if (count > 0) {
//            UserEntity targetEntity = userdataUserRepository.findByUsername(
//                    targetUser.username()
//            ).orElseThrow();
//
//            for (int i = 0; i < count; i++) {
//                String username = RandomDataUtils.randomUsername();
//
//                User addressee = xaTxTemplate.execute(() -> {
//                    AuthUserEntity authUser = authUserEntity(username, "12345");
//                    authUserRepository.create(authUser);
//                    UserEntity toUser = userdataUserRepository.create(userEntity(username));
//
//                    userdataUserRepository.sendInvitation(targetEntity, toUser);
//                    return User.fromEntity(toUser, null);
//                });
//                outcomeInvitations.add(addressee);
//            }
//        }
//        return outcomeInvitations;
//    }
//
   @Step("Create {1} outcome invitation using SQL")
    public List<AppUser> createOutcomeInvitations(AppUser targetUser, int count) {
        List<AppUser> outcomeInvitations = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findByUsername(
                    targetUser.username()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                String username = RandomDataUtils.randomUsername();

                AppUser invitee = xaTxTemplate.execute(() -> {
                    AppUser full = randomFullUserModel();                 // кого приглашаем
                    UserEntity toUser = createNewUser(full);

                    userdataUserRepository.sendInvitation(targetEntity, toUser);
                    return AppUser.fromEntity(toUser, null).withPassword(defaultPassword);
                });
                outcomeInvitations.add(invitee);
            }
        }
        return outcomeInvitations;
    }

//    @Step("Add {1} friends for userId using SQL INSERT")
//    public List<User> addFriends(User targetUser, int count) {
//        List<User> friends = new ArrayList<>();
//
//        if (count > 0) {
//            UserEntity targetEntity = userdataUserRepository.findByUsername(
//                    targetUser.username()
//            ).orElseThrow();
//
//            for (int i = 0; i < count; i++) {
//                String username = RandomDataUtils.randomUsername();
//
//                User friend = xaTxTemplate.execute(() -> {
//                    AuthUserEntity authUser = authUserEntity(username, "12345");
//                    authUserRepository.create(authUser);
//                    UserEntity userId = userdataUserRepository.create(userEntity(username));
//
//                    userdataUserRepository.addFriend(targetEntity, userId);
//                    return User.fromEntity(userId, null);
//                });
//                friends.add(friend);
//            }
//        }
//        return friends;
//    }
    @Step("Add {1} friends for userId using SQL INSERT")
    public List<AppUser> addFriends(AppUser targetUser, int count) {
        List<AppUser> friends = new ArrayList<>();

        if (count > 0) {
            UserEntity targetEntity = userdataUserRepository.findByUsername(
                    targetUser.username()
            ).orElseThrow();

            for (int i = 0; i < count; i++) {
                String username = RandomDataUtils.randomUsername();

                AppUser friend = xaTxTemplate.execute(() -> {
                    AppUser full = randomFullUserModel();                 // << генерация полной модели
                    UserEntity created = createNewUser(full);

                    userdataUserRepository.addFriend(targetEntity, created);
                    return AppUser.fromEntity(created, null).withPassword(defaultPassword);
                });
                friends.add(friend);
            }
        }
        return friends;
    }

    @Nonnull
    private UserEntity userEntity(String username) {
        UserEntity ue = new UserEntity();
        ue.setUsername(username);
        ue.setCountryCode("RU");
        return ue;
    }

//    @Nonnull
//    private UserEntity userEntity(User userId) {
//        UserEntity ue = new UserEntity();
//        ue.setUsername(userId.username());
//        ue.setFirstname(userId.firstname());
//        ue.setSurname(userId.surname());
//        ue.setAvatar(userId.avatar());
//        ue.setCountryCode(userId.countryCode());
//        return ue;
//    }

    @Nonnull
    private UserEntity createNewUser(String username, String password) {
        AuthUserEntity authUser = authUserEntity(username, password);
        authUserRepository.create(authUser);
        return userdataUserRepository.create(userEntity(username));
    }

    @Nonnull
    private UserEntity createNewUser(AppUser user) {
        AuthUserEntity authUser = authUserEntity(user.username(), user.testData().password());
        authUserRepository.create(authUser);
        UserEntity userEntity = UserEntity.from(user);

        // если small не прислали, но большой есть — сгенерим
        if (userEntity.getAvatarSmall() == null && isDataUrl(user.avatar())) {
            byte[] smallBytes = new SmallAvatar(75, 100, "png", user.avatar()).bytes();
            userEntity.setAvatarSmall(toRawBytesIfDataUrlBytes(smallBytes));
        }

        return userdataUserRepository.create(userEntity);
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

    // общий помощник: сгенерить ПОЛНУЮ модель юзера (имя/фамилия/страна/аватар/пароль)
    private AppUser randomFullUserModel() {
        UserData userData = UserDataGenerator.randomUser();  // country, first, last
        String avatar = RandomDataUtils.randomAvatar();  // data:image/png;base64,...

        return new AppUser(
                null,
                RandomDataUtils.randomUsername(),
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
                        new ArrayList<>(), // incomeInvitations
                        new ArrayList<>()  // outcomeInvitations
                )
        );
    }

    private static boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:image");
    }

    // Если bytes — это data-url в байтах, вернём RAW; иначе — как есть.
    private static byte[] toRawBytesIfDataUrlBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 5) return bytes;
        if (bytes[0]=='d' && bytes[1]=='a' && bytes[2]=='t' && bytes[3]=='a' && bytes[4]==':') {
            String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            int comma = s.indexOf(',');
            if (comma > 0) {
                return java.util.Base64.getDecoder().decode(s.substring(comma + 1));
            }
        }
        return bytes;
    }
}

