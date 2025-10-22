package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.data.entity.FriendshipEntity;
import ru.sentidas.rangiffler.data.entity.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.data.repository.FriendshipRepository;
import ru.sentidas.rangiffler.data.repository.UserRepository;
import ru.sentidas.rangiffler.ex.NotFoundException;
import ru.sentidas.rangiffler.ex.SameUsernameException;
import ru.sentidas.rangiffler.model.User;

import java.util.Date;
import java.util.UUID;

import static ru.sentidas.rangiffler.EventType.FRIEND_INVITE_ACCEPTED;
import static ru.sentidas.rangiffler.EventType.FRIEND_INVITE_SENT;
import static ru.sentidas.rangiffler.model.FriendshipStatus.*;
import static ru.sentidas.rangiffler.model.FriendshipStatus.INVITATION_SENT;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ActivityService activityService;

    @Transactional
    public User sendInvitation(String username, String targetId) {
        UserEntity initiator = getRequiredUser(username);
        UserEntity target = getRequiredUserById(UUID.fromString(targetId));

        //  запрет отправки самому себе
        if (initiator.getId().toString().equals(targetId)) {
            throw new SameUsernameException("Cannot create friendship request for self user");
        }

        // читаем обе стороны и классифицируем состояние
        Pair pair = getPair(initiator, target);
        Relation relation = classifyRelation(pair.a2b, pair.b2a);

        switch (relation) {
            case FRIEND -> {
                // Уже друзья → ничего не меняем, просто возвращаем нужный статус
                return User.fromEntity(target, FRIEND);
            }
            case INVITATION_SENT -> {
                // Исходящий уже существует → ничего не меняем
                return User.fromEntity(target, INVITATION_SENT);
            }
            case INVITATION_RECEIVED -> {
                // Есть входящий (от target к current) → ничего не меняем
                return User.fromEntity(target, INVITATION_RECEIVED);
            }
            case NONE -> {
                // Ничего нет → создаём PENDING A→B
                FriendshipEntity invite = new FriendshipEntity();
                invite.setRequester(initiator);
                invite.setAddressee(target);
                invite.setStatus(FriendshipStatus.PENDING);
                invite.setCreatedDate(new Date());
                friendshipRepository.save(invite);

                // Событие отправляем когда реально создан новый инвайт
                User initiatorDto = User.fromEntity(initiator);
                User targetDto = User.fromEntity(target);

                    activityService.publishFriendEvent(
                        FRIEND_INVITE_SENT,
                        initiatorDto.id(),
                        targetDto.id(),
                        targetDto.countryCode()
                );

                return User.fromEntity(target, INVITATION_SENT);
            }
            default -> {
                // сюда не попадаем, на всякий случай
                return User.fromEntity(target, null);
            }
        }
    }

    @Transactional
    public User acceptInvitation(String username, String targetId) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity inviter = getRequiredUserById(UUID.fromString(targetId));

        if (currentUser.getId().toString().equals(targetId)) {
            throw new SameUsernameException("Cannot accept friendship request for self user");
        }

        // найдём существующий инвайт (inviteUser -> currentUser)
        FriendshipEntity invite = friendshipRepository.findByRequesterAndAddressee(inviter, currentUser);
        if (invite == null) {
            throw new NotFoundException("Invitation not found");
        }

        // меняем статус входящего инвайта
        invite.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(invite);

        // 3) создаём обратную запись (currentUser -> inviteUser) и сохраняем
        FriendshipEntity reverse = friendshipRepository
                .findByRequesterAndAddressee(currentUser, inviter);
        if (reverse == null) {
            reverse = new FriendshipEntity();
            reverse.setRequester(currentUser);
            reverse.setAddressee(inviter);
            reverse.setStatus(FriendshipStatus.ACCEPTED);
            reverse.setCreatedDate(new Date());
            friendshipRepository.save(reverse);
        } else if (reverse.getStatus() != FriendshipStatus.ACCEPTED) {
            reverse.setStatus(FriendshipStatus.ACCEPTED);
            friendshipRepository.save(reverse);
        }

        User currentUserDto = User.fromEntity(currentUser);
        User targetUserDto = User.fromEntity(inviter);
        User result = User.fromEntity(inviter, FRIEND);

        //  добавляем: бизнес-событие для лога
        activityService.publishFriendEvent(
                FRIEND_INVITE_ACCEPTED,
                currentUserDto.id(),
                targetUserDto.id(),
                result.countryCode()
        );
        return result ;
    }

    @Transactional
    public User declineInvitation(String username, String targetId) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity inviteUser = getRequiredUserById(UUID.fromString(targetId));

        // берём обе стороны и классифицируем с точки зрения currentUser
        Pair pair = getPair(currentUser, inviteUser);
        Relation relation = classifyRelation(pair.a2b, pair.b2a);

        switch (relation) {
            case INVITATION_RECEIVED -> {
                // Был входящий B→A → удаляем его, отправляем событие, возвращаем target без статуса
                friendshipRepository.delete(pair.b2a);

                User currentUserJson = User.fromEntity(currentUser);
                User targetUserJson = User.fromEntity(inviteUser);

                activityService.publishFriendEvent(
                        ru.sentidas.rangiffler.EventType.FRIEND_INVITE_DECLINED,
                        currentUserJson.id(),
                        targetUserJson.id(),
                        targetUserJson.countryCode()
                );

                return User.fromEntity(inviteUser, null);
            }
            case INVITATION_SENT -> {
                // попытка отклонить свой же исходящий → ничего не делаем
                return User.fromEntity(inviteUser, INVITATION_SENT);
            }
            case FRIEND, NONE -> {
                // нет входящего инвайта → 404
                throw new NotFoundException("Invitation not found");
            }
            default -> {
                return User.fromEntity(inviteUser, null);
            }
        }
    }

    @Transactional
    public void deleteFriend(String username, String targetId) {
        UserEntity currentUser = getRequiredUser(username);
        UserEntity deletedUser = getRequiredUserById(UUID.fromString(targetId));

        // ищем и удаляем две записи с дружбой
        FriendshipEntity changeRequesterEntity =
                friendshipRepository.findByRequesterAndAddressee(currentUser, deletedUser);
        FriendshipEntity changeAddresseeEntity =
                friendshipRepository.findByRequesterAndAddressee(deletedUser, currentUser);

        if (changeRequesterEntity != null) {
            friendshipRepository.delete(changeRequesterEntity);
        }
        if (changeAddresseeEntity != null) {
            friendshipRepository.delete(changeAddresseeEntity);
        }
        User user = User.fromEntity(deletedUser, null);
        User currentUserJson = User.fromEntity(currentUser);
        User targetUserJson = User.fromEntity(deletedUser);

        activityService.publishFriendEvent(
                ru.sentidas.rangiffler.EventType.FRIEND_REMOVED,
                currentUserJson.id(),
                targetUserJson.id(),
                user.countryCode()
        );
    }


    @Nonnull
    private UserEntity getRequiredUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("Cannot find user by username: " + username)
        );
    }

    @Nonnull
    private UserEntity getRequiredUserById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Cannot find user by ID: " + id)
        );
    }

    @Nonnull
    UserEntity getRequiredUser(@Nonnull String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new NotFoundException("Cannot find user by username: " + username)
        );
    }

    // Локальная «пара» отношений между пользователями A и B.
    private static final class Pair {
        final FriendshipEntity a2b; // запись A -> B (может быть null)
        final FriendshipEntity b2a; // запись B -> A (может быть null)
        Pair(FriendshipEntity a2b, FriendshipEntity b2a) { this.a2b = a2b; this.b2a = b2a; }
    }

    // вернуть обе стороны дружбы сразу
    private Pair getPair(UserEntity a, UserEntity b) {
        FriendshipEntity a2b = friendshipRepository.findByRequesterAndAddressee(a, b);
        FriendshipEntity b2a = friendshipRepository.findByRequesterAndAddressee(b, a);
        return new Pair(a2b, b2a);
    }

    // Снимок состояния отношений между A и B с точки зрения пользователя A
    private enum Relation { FRIEND, INVITATION_SENT, INVITATION_RECEIVED, NONE }


    private Relation classifyRelation(FriendshipEntity a2b, FriendshipEntity b2a) {
        boolean aAccepted = a2b != null && a2b.getStatus() == FriendshipStatus.ACCEPTED;
        boolean bAccepted = b2a != null && b2a.getStatus() == FriendshipStatus.ACCEPTED;

        // Друзья если две стороны ACCEPTED
        if (aAccepted && bAccepted) {
            return Relation.FRIEND;
        }

        // Исходящий «от A к B»
        if (a2b != null && a2b.getStatus() == FriendshipStatus.PENDING) {
            return Relation.INVITATION_SENT;
        }
        // Входящий «от B к A»
        if (b2a != null && b2a.getStatus() == FriendshipStatus.PENDING) {
            return Relation.INVITATION_RECEIVED;
        }

        // Ни pending, ни accepted → NONE
        return Relation.NONE;
    }
}
