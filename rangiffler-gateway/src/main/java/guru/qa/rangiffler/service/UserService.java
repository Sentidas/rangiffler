package guru.qa.rangiffler.service;

import guru.qa.rangiffler.entity.CountryEntity;
import guru.qa.rangiffler.entity.FriendshipEntity;
import guru.qa.rangiffler.entity.StatisticEntity;
import guru.qa.rangiffler.entity.UserEntity;
import guru.qa.rangiffler.model.*;
import guru.qa.rangiffler.model.input.UserInput;
import guru.qa.rangiffler.repository.CountryRepository;
import guru.qa.rangiffler.repository.FriendShipRepository;
import guru.qa.rangiffler.repository.StatisticRepository;
import guru.qa.rangiffler.repository.UserRepository;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static guru.qa.rangiffler.model.FriendStatus.INVITATION_SENT;

@Component
public class UserService {

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final StatisticRepository statisticRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final FriendShipRepository friendShipRepository;

    @Autowired
    public UserService(UserRepository userRepository, CountryRepository countryRepository, StatisticRepository statisticRepository, FriendShipRepository friendShipRepository) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.statisticRepository = statisticRepository;
        this.friendShipRepository = friendShipRepository;
    }

    @Transactional(readOnly = true)
    public Page<User> allUsers(String excludedUsername, Pageable pageable, String searchQuery) {
        UserEntity currentUser = getUserEntity(excludedUsername);

        Page<UserEntity> page;

        if (searchQuery == null || searchQuery.isBlank()) {
            // Берём всех кроме excludedUser
            page = userRepository.findByIdNot(currentUser.getId(), pageable);
        } else {
            // Поиск с фильтром и исключением excludedUser
            page = userRepository.findByIdNotAndUsernameContainingIgnoreCaseOrFirstnameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
                    currentUser.getId(),
                    searchQuery,
                    searchQuery,
                    searchQuery,
                    pageable
            );
        }

        // Преобразуем в User DTO
        return page.map(userEntity ->
                User.fromUserEntity(userEntity, getFriendStatus(currentUser, userEntity)));
    }



    private FriendStatus getFriendStatus(UserEntity currentUser, UserEntity other) {
        if (currentUser.getFriendshipRequests() != null) {
            for (FriendshipEntity f : currentUser.getFriendshipRequests()) {
                if (f.getAddressee().equals(other)) {
                    return switch(f.getStatus()) {
                        case PENDING -> INVITATION_SENT;
                        case ACCEPTED -> FriendStatus.FRIEND;
                    };
                }
            }
        }

        if (currentUser.getFriendshipAddressees() != null) {
            for (FriendshipEntity f : currentUser.getFriendshipAddressees()) {
                if (f.getRequester().equals(other)) {
                    return switch(f.getStatus()) {
                        case PENDING -> FriendStatus.INVITATION_RECEIVED;
                        case ACCEPTED -> FriendStatus.FRIEND;
                    };
                }
            }
        }

        return null;
    }


    public User getUser(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return User.fromUserEntity(user);
    }

    public UserEntity getUserEntity(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }

    @Transactional(readOnly = true)
    public Slice<User> friends (String username, Pageable pageable, String searchQuery) {
        UserEntity user = getUserEntity(username);
        if(searchQuery == null) {
            return userRepository.findFriends(
                    user,
                    pageable).map(friend -> User.fromUserEntity(friend, FriendStatus.FRIEND));

        } else {
            return userRepository.findFriends(
                    user,
                    pageable,
                    searchQuery
            ).map(friend -> User.fromUserEntity(friend, FriendStatus.FRIEND));
        }
    }
    @Transactional(readOnly = true)
    public @Nonnull
    Slice<User> incomeInvitations(@Nonnull String username,
                                     @Nonnull Pageable pageable,
                                     @Nullable String searchQuery) {
        return searchQuery == null
                ? userRepository.findIncomeInvitations(
                getUserEntity(username),
                pageable
        ).map(i -> User.fromUserEntity(i, FriendStatus.INVITATION_RECEIVED))
                : userRepository.findIncomeInvitations(
                getUserEntity(username),
                pageable,
                searchQuery
        ).map(i -> User.fromUserEntity(i, FriendStatus.INVITATION_RECEIVED));
    }

    @Transactional(readOnly = true)
    public @Nonnull
    Slice<User> outcomeInvitations(@Nonnull String username,
                                      @Nonnull Pageable pageable,
                                      @Nullable String searchQuery) {
        return searchQuery == null
                ? userRepository.findOutcomeInvitations(
                getUserEntity(username),
                pageable
        ).map(o -> User.fromUserEntity(o, INVITATION_SENT))
                : userRepository.findOutcomeInvitations(
                getUserEntity(username),
                pageable,
                searchQuery
        ).map(o -> User.fromUserEntity(o, INVITATION_SENT));
    }



    @Transactional(readOnly = true)
    public List<Stat> stat(@Nonnull String username, boolean withFriends) {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<StatisticEntity> stats = statisticRepository.findAllByUserIn(
                List.of(userEntity));


        return stats.stream().map(
                se -> new Stat(
                        se.getCount(),
                        Country.fromEntity(se.getCountry())
                )
        ).toList();
    }


    @Transactional
    public User updateUser(String username, UserInput input) {

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (input.firstname() != null) {
            userEntity.setFirstname(input.firstname());
        }

        if (input.surname() != null) {
            userEntity.setSurname(input.surname());
        }

        if (input.avatar() != null) {
            // Убедитесь, что это Data URL и извлеките base64 часть
            if (input.avatar().startsWith("data:")) {
                String base64Data = input.avatar().substring(input.avatar().indexOf(",") + 1);
                userEntity.setAvatar(Base64.getDecoder().decode(base64Data));
            } else {
                // Если это не Data URL, сохраняем как есть (в байтах)
                userEntity.setAvatar(input.avatar().getBytes(StandardCharsets.UTF_8));
            }
        }

        if (input.location() != null && input.location().code() != null) {
            CountryEntity countryEntity = countryRepository.findByCode(input.location().code())
                    .orElseThrow(() -> new RuntimeException("Country not found with code: " + input.location().code()));

            userEntity.setCountry(countryEntity);
        }

        UserEntity updateUser = userRepository.save(userEntity);
        return User.fromUserEntity(updateUser);
    }


    @Transactional
    @KafkaListener(topics = "users1", groupId = "gateway")
    public void listener(@Payload UserJson user, ConsumerRecord<String, UserJson> cr) {
        userRepository.findByUsername(user.username())
                .ifPresentOrElse(
                        u -> LOG.info("### User already exist in DB, kafka event will be skipped: {}", cr.toString()),
                        () -> {
                            LOG.info("### Kafka consumer record: {}", cr.toString());

                            CountryEntity countryEntity = countryRepository.findByCode("cy")
                                    .orElseThrow(() -> new RuntimeException("Default country not found"));

                            UserEntity userDataEntity = new UserEntity();
                            userDataEntity.setUsername(user.username());
                            userDataEntity.setCountry(countryEntity);

                            UserEntity userEntity = userRepository.save(userDataEntity);

                            LOG.info(
                                    "### User '{}' successfully saved to database with id: {}",
                                    user.username(),
                                    userEntity.getId()
                            );
                        }
                );
    }

    public User addFriend(String username, UUID friendId) {
        FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setRequester(getUserEntity(username));
        UserEntity addressee = userRepository.findById(friendId).orElseThrow();
        friendshipEntity.setAddressee(addressee);
        friendshipEntity.setCreatedDate(new Date());
        friendshipEntity.setStatus(FriendshipStatus.PENDING);
        FriendshipEntity saved = friendShipRepository.save(friendshipEntity);
        return User.fromUserEntity(addressee, INVITATION_SENT);
    }

    public User acceptInvitation(String username, UUID friendId) {
        // ищем entity отправителя
        UserEntity requesterEntity = userRepository.findById(friendId).orElseThrow();//duck
        // ищем entity владельца аккаунта, принимающего заявку
        UserEntity addresseEntity = getUserEntity(username); // fox
        // меняем статус дружбы у отправителя c PENDING на FRIEND
        FriendshipEntity changeRequesterEntity = friendShipRepository.findByRequesterAndAddressee(requesterEntity, addresseEntity);
        changeRequesterEntity.setStatus(FriendshipStatus.ACCEPTED);
        friendShipRepository.save(changeRequesterEntity);


        // создаем дружбу наоборот со статусом FRIEND
        FriendshipEntity friendshipEntity = new FriendshipEntity();
        friendshipEntity.setRequester(addresseEntity); // fox
        friendshipEntity.setAddressee(requesterEntity); // duck
        friendshipEntity.setCreatedDate(new Date());
        friendshipEntity.setStatus(FriendshipStatus.ACCEPTED);

        // сохраняем запись в бд
        FriendshipEntity saved = friendShipRepository.save(friendshipEntity);
        return User.fromUserEntity(addresseEntity, FriendStatus.FRIEND);
    }

    public User rejectInvitation(String username, UUID friendId) {
        // ищем entity отправителя
        UserEntity requesterEntity = userRepository.findById(friendId).orElseThrow();//duck
        // ищем entity владельца аккаунта, принимающего заявку
        UserEntity addresseEntity = getUserEntity(username); // fox
        // меняем статус дружбы у отправителя c PENDING на FRIEND
        FriendshipEntity changeRequesterEntity = friendShipRepository.findByRequesterAndAddressee(requesterEntity, addresseEntity);


        friendShipRepository.delete(changeRequesterEntity);



        return User.fromUserEntity(addresseEntity, FriendStatus.NOT_FRIEND);
    }

    public User deleteFriend(String username, UUID friendId) {
        // ищем entity отправителя
        UserEntity requesterEntity = userRepository.findById(friendId).orElseThrow();//duck
        // ищем entity владельца аккаунта, принимающего заявку
        UserEntity addresseEntity = getUserEntity(username); // fox
        // меняем статус дружбы у отправителя c PENDING на FRIEND
        FriendshipEntity changeRequesterEntity = friendShipRepository.findByRequesterAndAddressee(requesterEntity, addresseEntity);

        FriendshipEntity changeAddresseeEntity = friendShipRepository.findByRequesterAndAddressee(addresseEntity, requesterEntity);

        friendShipRepository.delete(changeRequesterEntity);
        friendShipRepository.delete(changeAddresseeEntity);


        return User.fromUserEntity(addresseEntity, FriendStatus.NOT_FRIEND);
    }
}
