package guru.qa.rangiffler.data.repository;

import guru.qa.rangiffler.data.UserEntity;
import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

  @Nonnull
  Optional<UserEntity> findByUsername(@Nonnull String username);
}
