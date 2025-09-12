package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.data.entity.FriendshipEntity;
import ru.sentidas.rangiffler.data.entity.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.data.projection.UserWithStatus;
import ru.sentidas.rangiffler.data.repository.FriendshipRepository;
import ru.sentidas.rangiffler.data.repository.UserRepository;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.ex.SameUsernameException;
import ru.sentidas.rangiffler.model.User;
import ru.sentidas.rangiffler.model.UserBulk;
import ru.sentidas.rangiffler.model.UserEvent;

import java.util.*;


@Component
@Transactional
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    public static final String DEFAULT_USER_COUNTRY = "ru";

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;


    @Autowired
    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    @KafkaListener(topics = "rangiffler_user", groupId = "userdata")
    public void listener(@Payload UserEvent user, ConsumerRecord<String, UserEvent> cr) {
        userRepository.findByUsername(user.username())
                .ifPresentOrElse(
                        u -> LOG.info("### User already exist in DB, kafka event will be skipped: {}", cr.toString()),
                        () -> {
                            LOG.info("### Kafka consumer record: {}", cr.toString());

                            UserEntity userDataEntity = new UserEntity();
                            userDataEntity.setUsername(user.username());
                            userDataEntity.setCountryCode(DEFAULT_USER_COUNTRY);

                            UserEntity userEntity = userRepository.save(userDataEntity);

                            LOG.info(
                                    "### User '{}' successfully saved to database with id: {}",
                                    user.username(),
                                    userEntity.getId()
                            );
                        }
                );
    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found " + username));

    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found " + userId));

    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        return User.fromEntity(getUserEntity(username));
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return User.fromEntity(getUserEntityById(userId));
    }


//    @Transactional(readOnly = true)
//    public Page<UserBulk> allUsers(String excludedUsername, Pageable pageable, String searchQuery) {
//        UserEntity currentUser = getUserEntity(excludedUsername);
//
//        Page<UserEntity> page;
//
//        if (searchQuery == null || searchQuery.isBlank()) {
//            // Берём всех кроме excludedUser
//            page = userRepository.findByIdNot(currentUser.getId(), pageable);
//        } else {
//            // Поиск с фильтром и исключением excludedUser
//            page = userRepository.findByIdNotAndUsernameContainingIgnoreCaseOrFirstnameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
//                    currentUser.getId(),
//                    searchQuery,
//                    searchQuery,
//                    searchQuery,
//                    pageable
//            );
//        }
//
//        // Преобразуем в User DTO
//        return page.map(userEntity ->
//                UserBulk.fromUserEntityProjection(userEntity, getFriendStatus(currentUser, userEntity)));
//    }

    @Transactional(readOnly = true)
    public Page<UserBulk> allUsers(String excludedUsername, Pageable pageable, String searchQuery) {
        UserEntity current = getUserEntity(excludedUsername);

        Page<UserWithStatus> page = (searchQuery == null || searchQuery.isBlank())
                ? userRepository.findUsersWithStatus(current, pageable)
                : userRepository.findUsersWithStatus(current, searchQuery, pageable);

        return page.map(UserBulk::fromUserEntityProjection);
    }

//    @Transactional(readOnly = true)
//    public @Nonnull
//    Slice<UserBulk> incomeInvitations(@Nonnull String username,
//                                      @Nonnull Pageable pageable,
//                                      @Nullable String searchQuery) {
//        return searchQuery == null
//                ? userRepository.findIncomeInvitations(
//                getRequiredUser(username),
//                pageable
//        ).map(i -> UserBulk.fromEntity(i, FriendStatus.INVITATION_RECEIVED))
//                : userRepository.findIncomeInvitations(
//                getRequiredUser(username),
//                pageable,
//                searchQuery
//        ).map(i -> UserBulk.fromEntity(i, FriendStatus.INVITATION_RECEIVED));
//    }
//
//    @Transactional(readOnly = true)
//    public @Nonnull
//    Slice<UserBulk> outcomeInvitations(@Nonnull String username,
//                                       @Nonnull Pageable pageable,
//                                       @Nullable String searchQuery) {
//        return searchQuery == null
//                ? userRepository.findOutcomeInvitations(
//                getRequiredUser(username),
//                pageable
//        ).map(o -> UserBulk.fromEntity(o, INVITATION_SENT))
//                : userRepository.findOutcomeInvitations(
//                getRequiredUser(username),
//                pageable,
//                searchQuery
//        ).map(o -> UserBulk.fromEntity(o, INVITATION_SENT));
//    }
//
//
//    @Transactional(readOnly = true)
//    public Slice<UserBulk> friends(String username, Pageable pageable, String searchQuery) {
//        UserEntity user = getUserEntity(username);
//
//        if (searchQuery == null) {
//            return userRepository.findFriends(
//                    user,
//                    pageable).map(friend -> UserBulk.fromEntity(friend, FriendStatus.FRIEND));
//
//        } else {
//            return userRepository.findFriends(
//                    user,
//                    pageable,
//                    searchQuery
//            ).map(friend -> UserBulk.fromEntity(friend, FriendStatus.FRIEND));
//        }
//    }

    public Slice<UserBulk> incomeInvitations(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findIncomeInvitationsProjection(current, pageable)
                : userRepository.findIncomeInvitationsProjection(current, q, pageable);
        return s.map(UserBulk::fromFriendEntityProjection);
    }

    public Slice<UserBulk> outcomeInvitations(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findOutcomeInvitationsProjection(current, pageable)
                : userRepository.findOutcomeInvitationsProjection(current, q, pageable);
        return s.map(UserBulk::fromUserEntityProjection);
    }

    public Slice<UserBulk> friends(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findFriendsProjection(current, pageable)
                : userRepository.findFriendsProjection(current, q, pageable);
        return s.map(u -> UserBulk.fromFriendEntityProjection(u)); // статус FRIEND
    }

    @Transactional(readOnly = true)
    public List<UUID> friendsId(String username) {
        UserEntity user = getUserEntity(username);

        List<UserEntity> friends = userRepository.findFriends(user);
        return friends.stream().map(UserEntity::getId).toList();
    }


//    private FriendStatus getFriendStatus(UserEntity currentUser, UserEntity other) {
//        if (currentUser.getFriendshipRequests() != null) {
//            for (FriendshipEntity f : currentUser.getFriendshipRequests()) {
//                if (f.getAddressee().equals(other)) {
//                    return switch (f.getStatus()) {
//                        case PENDING -> INVITATION_SENT;
//                        case ACCEPTED -> FriendStatus.FRIEND;
//                    };
//                }
//            }
//        }
//
//        if (currentUser.getFriendshipAddressees() != null) {
//            for (FriendshipEntity f : currentUser.getFriendshipAddressees()) {
//                if (f.getRequester().equals(other)) {
//                    return switch (f.getStatus()) {
//                        case PENDING -> FriendStatus.INVITATION_RECEIVED;
//                        case ACCEPTED -> FriendStatus.FRIEND;
//                    };
//                }
//            }
//        }
//
//        return null;
//    }

    @Transactional
    public User update(User user) {
        UserEntity userEntity = userRepository.findByUsername(user.username())
                .orElseGet(() -> {
                    UserEntity emptyUser = new UserEntity();
                    emptyUser.setUsername(user.username());
                    emptyUser.setCountryCode("RU");
                    return emptyUser;
                });

        if (user.firstname() != null) userEntity.setFirstname(user.firstname());
        if (user.surname() != null) userEntity.setSurname(user.surname());
        if (user.countryCode() != null) userEntity.setCountryCode(user.countryCode());

        if (user.avatar() != null && isDataUrl(user.avatar())) {
            applyAvatar(userEntity, user.avatar());
        }

        UserEntity updateUser = userRepository.save(userEntity);
        return User.fromEntity(updateUser);
    }

    private static boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:image");
    }

    private static void applyAvatar(UserEntity ue, String avatarDataUrl) {
        if (!isDataUrl(avatarDataUrl)) return;

        // вытащили base64 из data-url
        String base64 = avatarDataUrl.substring(avatarDataUrl.indexOf(',') + 1);
        byte[] decoded = java.util.Base64.getDecoder().decode(base64);

        // ОРИГИНАЛ: перекодируем в PNG и храним RAW-байты PNG
        try (var is = new java.io.ByteArrayInputStream(decoded);
             var os = new java.io.ByteArrayOutputStream()) {
            net.coobird.thumbnailator.Thumbnails.of(javax.imageio.ImageIO.read(is))
                    .scale(1.0)            // без изменения размера, только перекодирование
                    .outputFormat("png")
                    .toOutputStream(os);
            ue.setAvatar(os.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to normalize avatar to PNG", e);
        }

        // SMALL: как и раньше, через SmallAvatar (он уже отдаёт PNG-байты)
        ue.setAvatarSmall(new SmallAvatar(100, 100, avatarDataUrl).bytes());
    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("Can`t find user by username: '" + username + "'")
        );
    }

    @Nonnull
    UserEntity getRequiredUserById(@Nonnull UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Can`t find user by Id: '" + id + "'")
        );

    }


    public User sendInvitation(String username, String targetId) {
        if (Objects.equals(username, targetId)) {
            throw new SameUsernameException("Can`t create friendship request for self user");
        }
        UserEntity currentUser = getRequiredUser(username);
        UserEntity targetUser = getRequiredUserById(UUID.fromString(targetId));

        FriendshipEntity invite = new FriendshipEntity();
        invite.setRequester(currentUser);
        invite.setAddressee(targetUser);
        invite.setStatus(FriendshipStatus.PENDING);
        invite.setCreatedDate(new Date());

        friendshipRepository.save(invite);   // <-- сохраняем сам инвайт
        return User.fromEntity(targetUser, ru.sentidas.rangiffler.model.FriendshipStatus.INVITATION_SENT);

    }

    public User acceptInvitation(String username, String targetId) {
        if (Objects.equals(username, targetId)) {
            throw new SameUsernameException("Can`t accept friendship request for self user");
        } // fix проверить, возможно подходит только для rest

        UserEntity currentUser = getRequiredUser(username);
        UserEntity inviteUser = getRequiredUserById(UUID.fromString(targetId));

        // найдём существующий инвайт (inviteUser -> currentUser)
        FriendshipEntity invite = friendshipRepository
                .findByRequesterAndAddressee(inviteUser, currentUser);
        if (invite == null) {
            throw new NotFoundException("Invitation not found");
        }

        // меняем статус входящего инвайта
        invite.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(invite);

        // 3) создаём (если нет) обратную запись (currentUser -> inviteUser) и тоже сохраняем
        FriendshipEntity reverse = friendshipRepository
                .findByRequesterAndAddressee(currentUser, inviteUser);
        if (reverse == null) {
            reverse = new FriendshipEntity();
            reverse.setRequester(currentUser);
            reverse.setAddressee(inviteUser);
            reverse.setStatus(FriendshipStatus.ACCEPTED);
            reverse.setCreatedDate(new Date());
            friendshipRepository.save(reverse);
        } else if (reverse.getStatus() != FriendshipStatus.ACCEPTED) {
            reverse.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(reverse);
        }
        return User.fromEntity(inviteUser, ru.sentidas.rangiffler.model.FriendshipStatus.FRIEND);
    }

    public User declineInvitation(String username, String targetId) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity inviteUser = getRequiredUserById(UUID.fromString(targetId));

        FriendshipEntity invite = friendshipRepository
                .findByRequesterAndAddressee(inviteUser, currentUser);

        if (invite == null) {
            // ничего не делаем/или 404
            throw new NotFoundException("Invitation not found");
        }

        friendshipRepository.delete(invite);

        return User.fromEntity(inviteUser, null);
    }

    public void deleteFriend(String username, String targetId) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity deletedUser = getRequiredUserById(UUID.fromString(targetId));

        // ищем и удаляем две записи с дружбой
        FriendshipEntity changeRequesterEntity = friendshipRepository.findByRequesterAndAddressee(currentUser, deletedUser);
        FriendshipEntity changeAddresseeEntity = friendshipRepository.findByRequesterAndAddressee(deletedUser, currentUser);

        if (changeRequesterEntity != null) {
            friendshipRepository.delete(changeRequesterEntity);
        }
        if (changeAddresseeEntity != null) {
            friendshipRepository.delete(changeAddresseeEntity);
        }
        User.fromEntity(deletedUser, null);
    }
}
