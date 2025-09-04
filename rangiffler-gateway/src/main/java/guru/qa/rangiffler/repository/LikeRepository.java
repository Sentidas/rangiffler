package guru.qa.rangiffler.repository;

import guru.qa.rangiffler.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikeRepository extends JpaRepository<LikeEntity, UUID> {

}
