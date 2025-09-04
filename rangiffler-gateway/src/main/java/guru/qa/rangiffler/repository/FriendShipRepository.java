package guru.qa.rangiffler.repository;

import guru.qa.rangiffler.entity.FriendshipEntity;
import guru.qa.rangiffler.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendShipRepository extends JpaRepository<FriendshipEntity, UUID> {

    FriendshipEntity findByRequesterAndAddressee(UserEntity requester, UserEntity addressee);
}
