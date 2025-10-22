package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.data.projection.UserWithStatus;
import ru.sentidas.rangiffler.data.repository.UserRepository;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.model.StorageType;
import ru.sentidas.rangiffler.model.User;
import ru.sentidas.rangiffler.model.UserBulk;
import ru.sentidas.rangiffler.model.UserEvent;

import java.util.*;

import static ru.sentidas.rangiffler.model.StorageType.valueOf;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final String DEFAULT_USER_COUNTRY = "ru";

    private final UserRepository userRepository;
    private final AvatarService avatarService;

    @Value("${app.media.storage.default:BLOB}")
    private String storageMode;

    // Определяет режим хранения image по конфигу
    private StorageType currentStorageMode() {
        try {
            return valueOf(storageMode.toUpperCase());
        } catch (Exception e) {
            LOG.warn("Unknown storage mode '{}', fallback to BLOB", storageMode);
            return StorageType.BLOB;
        }
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
                            userDataEntity.setStorage(currentStorageMode());

                            UserEntity userEntity = userRepository.save(userDataEntity);

                            LOG.info(
                                    "### User '{}' successfully saved to database with id: {}",
                                    user.username(),
                                    userEntity.getId()
                            );
                        }
                );
    }

    // ===== ЧТЕНИЕ =====

    @Transactional(readOnly = true)
    public UserEntity getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cannot find user by username: '" + username));

    }

    @Transactional(readOnly = true)
    public UserEntity getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Cannot find user by ID: " + userId));

    }

    @Transactional(readOnly = true)
    public Map<UUID, String> usernamesByIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        Iterable<UserEntity> users = userRepository.findAllById(userIds);

        Map<UUID, String> byId = new HashMap<>();
        for (UserEntity u : users) {
            if (u.getId() != null && u.getUsername() != null) {
                byId.put(u.getId(), u.getUsername());
            }
        }

        Map<UUID, String> ordered = new LinkedHashMap<>();
        for (UUID id : userIds) {
            String username = byId.get(id);
            if (username != null) {
                ordered.put(id, username);
            }
        }
        return ordered;
    }

    @Transactional(readOnly = true)
    public User getUser(String username) {
        UserEntity userEntity = getUserEntity(username);
        return toUser(userEntity);
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        UserEntity userEntity = getUserEntityById(userId);
        return toUser(userEntity);
    }


    @Transactional(readOnly = true)
    public Page<UserBulk> allUsers(String excludedUsername, Pageable pageable, String searchQuery) {
        UserEntity current = getUserEntity(excludedUsername);

        Page<ru.sentidas.rangiffler.data.projection.UserWithBiStatus> page =
                (searchQuery == null || searchQuery.isBlank())
                        ? userRepository.findUsersWithBiStatus(current, pageable)
                        : userRepository.findUsersWithBiStatus(current, searchQuery, pageable);

        return page.map(UserBulk::fromUserBiProjection);
    }

    @Transactional(readOnly = true)
    public Slice<UserBulk> incomeInvitations(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findIncomeInvitationsProjection(current, pageable)
                : userRepository.findIncomeInvitationsProjection(current, q, pageable);
        return s.map(UserBulk::fromFriendEntityProjection);
    }

    @Transactional(readOnly = true)
    public Slice<UserBulk> outcomeInvitations(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findOutcomeInvitationsProjection(current, pageable)
                : userRepository.findOutcomeInvitationsProjection(current, q, pageable);
        return s.map(UserBulk::fromUserEntityProjection);
    }

    @Transactional(readOnly = true)
    public Slice<UserBulk> friends(String username, Pageable pageable, String q) {
        UserEntity current = getRequiredUser(username);
        Slice<UserWithStatus> s = (q == null)
                ? userRepository.findFriendsProjection(current, pageable)
                : userRepository.findFriendsProjection(current, q, pageable);
        return s.map(UserBulk::fromFriendEntityProjection);
    }

    @Transactional(readOnly = true)
    public List<UUID> friendsId(String username) {
        UserEntity user = getUserEntity(username);

        List<UserEntity> friends = userRepository.findFriends(user);
        return friends.stream().map(UserEntity::getId).toList();
    }

    // ===== ЗАПИСЬ =====

    @Transactional
    public User update(User user) {
        // 1) гарантируем, что пользователь есть: пробуем вставить «минимального»
        UserEntity userEntity = userRepository.findByUsername(user.username())
                .orElseGet(() -> {
                    UserEntity created = new UserEntity();
                    created.setUsername(user.username());
                    created.setCountryCode(DEFAULT_USER_COUNTRY);
                    created.setStorage(currentStorageMode());
                    try {
                        return userRepository.save(created);
                    } catch (ConstraintViolationException
                             | DataIntegrityViolationException e) {
                        // если кто-то успел вставить раньше — читаем уже существующую запись
                        return userRepository.findByUsername(user.username())
                                .orElseThrow(() ->
                                        new NotFoundException("User exists but not readable: " + user.username()));
                    }
                });

        if (user.firstname() != null) userEntity.setFirstname(user.firstname());
        if (user.surname() != null) userEntity.setSurname(user.surname());
        if (user.countryCode() != null) userEntity.setCountryCode(user.countryCode());

        // гарантируем, что у сущности есть id перед работой с аватаром/MinIO
        userEntity = userRepository.save(userEntity);

        if (user.avatar() != null) {
            if (isDataUrl(user.avatar())) {
                StorageType target = currentStorageMode();
                StorageType previous = userEntity.getStorage();

                AvatarService.Result result = avatarService.process(
                        userEntity.getId(),
                        user.avatar(),
                        target,
                        userEntity.getObjectKey() // старый ключ пригодится, если был OBJECT -> OBJECT
                );

                if (target == StorageType.OBJECT) {
                    // Сохраняем в OBJECT (MinIO), BLOB-байты не храним
                    userEntity.setStorage(StorageType.OBJECT);
                    // если раньше был OBJECT и ключ сменился — AvatarService вернёт deleteOldObjectKey
                    userEntity.setObjectKey(result.objectKey());
                    userEntity.setAvatar(null);
                    userEntity.setMime(result.mime());
                    userEntity.setAvatarSmall(result.avatarSmallBytes());
                    avatarService.deleteObjectIfNeeded(result.deleteOldObjectKey());

                    // Если раньше был BLOB — перезаписываем на OBJECT; чистить нечего
                } else {
                    // Сохраняем в BLOB, удаляем объект из MinIO если уходили с OBJECT
                    if (previous == StorageType.OBJECT) {
                        avatarService.deleteObjectIfNeeded(userEntity.getObjectKey());
                    }
                    userEntity.setStorage(StorageType.BLOB);
                    userEntity.setObjectKey(null);
                    userEntity.setAvatar(result.avatarBytes());
                    userEntity.setMime(result.mime());
                    userEntity.setAvatarSmall(result.avatarSmallBytes());
                }
            } else {
                LOG.debug("Avatar not updated for '{}': non-data URL received (frontend echo).", user.username());
            }
        }

        UserEntity saved = userRepository.save(userEntity);
        return toUser(saved);
    }

    private User toUser(UserEntity userEntity) {
        String avatar = null;

        if (userEntity.getStorage() == StorageType.OBJECT) {
            // для детальной карточки фронт ждёт objectKey, а не dataURL
            avatar = userEntity.getObjectKey();
        } else if (userEntity.getAvatar() != null && userEntity.getAvatar().length > 0) {
            String mime = (userEntity.getMime() == null || userEntity.getMime().isBlank())
                    ? "image/png"
                    : userEntity.getMime();
            avatar = DataUrl.build(mime, userEntity.getAvatar());
        }

        String countryCode = userEntity.getCountryCode() != null
                ? userEntity.getCountryCode().toLowerCase(java.util.Locale.ROOT)
                : null;

        return new User(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getFirstname(),
                userEntity.getSurname(),
                avatar,
                null, // friend_status назначается на уровне вызова списков/страниц
                countryCode
        );
    }

    private static boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:image");
    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("Cannot find user by username: " + username)
        );
    }
}
