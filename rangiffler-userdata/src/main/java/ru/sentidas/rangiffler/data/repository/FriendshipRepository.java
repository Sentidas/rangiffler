package ru.sentidas.rangiffler.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.sentidas.rangiffler.data.entity.FriendShipId;
import ru.sentidas.rangiffler.data.entity.FriendshipEntity;
import ru.sentidas.rangiffler.data.entity.UserEntity;

public interface FriendshipRepository extends JpaRepository<FriendshipEntity, FriendShipId> {
    FriendshipEntity findByRequesterAndAddressee(UserEntity requester, UserEntity addressee);
}
