package ru.sentidas.rangiffler.data.repository;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sentidas.rangiffler.data.entity.UserEntity;
import ru.sentidas.rangiffler.data.projection.UserWithStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(@Nonnull String username);

    Slice<UserEntity> findByUsernameNot(@Nonnull String username,
                                        @Nonnull Pageable pageable);

    @Query("select u from UserEntity u where u.username <> :username" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    Slice<UserEntity> findByUsernameNotAndSearchQuery(@Param("username") String username,
                                                      @Nonnull Pageable pageable,
                                                      @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED and f.requester = :requester")
    Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                  @Nonnull Pageable pageable);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED and f.requester = :requester" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                  @Nonnull Pageable pageable,
                                  @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED and f.requester = :requester")
    List<UserEntity> findFriends(@Param("requester") UserEntity requester);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED and f.requester = :requester" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    Slice<UserEntity> findFriends(@Param("requester") UserEntity requester,
                                  @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.requester = :requester")
    Slice<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                             @Nonnull Pageable pageable);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.requester = :requester" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    Slice<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                             @Nonnull Pageable pageable,
                                             @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.requester = :requester")
    List<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.addressee" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.requester = :requester" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    List<UserEntity> findOutcomeInvitations(@Param("requester") UserEntity requester,
                                            @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.addressee = :addressee")
    Slice<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                            @Nonnull Pageable pageable);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.addressee = :addressee" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    Slice<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                            @Nonnull Pageable pageable,
                                            @Param("searchQuery") String searchQuery);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.addressee = :addressee")
    List<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee);

    @Query("select u from UserEntity u join FriendshipEntity f on u = f.requester" +
            " where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING and f.addressee = :addressee" +
            " and (u.username like %:searchQuery% or u.firstname like %:searchQuery% or u.surname like %:searchQuery%)")
    List<UserEntity> findIncomeInvitations(@Param("addressee") UserEntity addressee,
                                           @Param("searchQuery") String searchQuery);



    // Поиск с фильтром и исключением
    Page<UserEntity> findByIdNotAndUsernameContainingIgnoreCaseOrFirstnameContainingIgnoreCaseOrSurnameContainingIgnoreCase(
            UUID excludedUserId,
            String username,
            String firstname,
            String surname,
            Pageable pageable
    );

    Page<UserEntity> findByIdNot(UUID id, Pageable pageable);

    // === ALL USERS (кроме текущего) + статус от текущего пользователя к каждому ===
    @Query("""
select distinct new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
left join FriendshipEntity f
  on (u = f.addressee and f.requester = :current)
where u <> :current
""")
    Page<UserWithStatus> findUsersWithStatus(@Param("current") UserEntity current, Pageable pageable);

    @Query("""
select distinct new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
left join FriendshipEntity f
  on (u = f.addressee and f.requester = :current)
where u <> :current and (
  lower(u.username) like lower(concat('%', :q, '%')) or
  lower(u.firstname) like lower(concat('%', :q, '%')) or
  lower(u.surname) like lower(concat('%', :q, '%'))
)
""")
    Page<UserWithStatus> findUsersWithStatus(@Param("current") UserEntity current,
                                             @Param("q") String q,
                                             Pageable pageable);

    // === FRIENDS (accepted) ===
    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.addressee
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED
  and f.requester = :current
""")
    Slice<UserWithStatus> findFriendsProjection(@Param("current") UserEntity current, Pageable pageable);

    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.addressee
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.ACCEPTED
  and f.requester = :current
  and (lower(u.username) like lower(concat('%', :q, '%'))
       or lower(u.firstname) like lower(concat('%', :q, '%'))
       or lower(u.surname) like lower(concat('%', :q, '%')))
""")
    Slice<UserWithStatus> findFriendsProjection(@Param("current") UserEntity current,
                                                @Param("q") String q,
                                                Pageable pageable);

    // === INCOME (pending, где current — addressee) ===
    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.requester
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
  and f.addressee = :current
""")
    Slice<UserWithStatus> findIncomeInvitationsProjection(@Param("current") UserEntity current, Pageable pageable);

    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.requester
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
  and f.addressee = :current
  and (lower(u.username) like lower(concat('%', :q, '%'))
       or lower(u.firstname) like lower(concat('%', :q, '%'))
       or lower(u.surname) like lower(concat('%', :q, '%')))
""")
    Slice<UserWithStatus> findIncomeInvitationsProjection(@Param("current") UserEntity current,
                                                          @Param("q") String q,
                                                          Pageable pageable);

    // === OUTCOME (pending, где current — requester) ===
    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.addressee
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
  and f.requester = :current
""")
    Slice<UserWithStatus> findOutcomeInvitationsProjection(@Param("current") UserEntity current, Pageable pageable);

    @Query("""
select new ru.sentidas.rangiffler.data.projection.UserWithStatus(
  u.id, u.username, u.firstname, u.surname, u.avatarSmall, f.status, u.countryCode
)
from UserEntity u
join FriendshipEntity f on u = f.addressee
where f.status = ru.sentidas.rangiffler.data.entity.FriendshipStatus.PENDING
  and f.requester = :current
  and (lower(u.username) like lower(concat('%', :q, '%'))
       or lower(u.firstname) like lower(concat('%', :q, '%'))
       or lower(u.surname) like lower(concat('%', :q, '%')))
""")
    Slice<UserWithStatus> findOutcomeInvitationsProjection(@Param("current") UserEntity current,
                                                           @Param("q") String q,
                                                           Pageable pageable);
}


