package ru.sentidas.rangiffler.service;

import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.data.Authority;
import ru.sentidas.rangiffler.data.AuthorityEntity;
import ru.sentidas.rangiffler.data.UserEntity;
import ru.sentidas.rangiffler.data.repository.UserRepository;
import ru.sentidas.rangiffler.model.UserEvent;

import java.util.UUID;

@Component
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public @Nonnull
    String registerUser(@Nonnull String username, @Nonnull String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEnabled(true);
        userEntity.setAccountNonExpired(true);
        userEntity.setCredentialsNonExpired(true);
        userEntity.setAccountNonLocked(true);
        userEntity.setUsername(username);
        userEntity.setPassword(passwordEncoder.encode(password));

        AuthorityEntity readAuthorityEntity = new AuthorityEntity();
        readAuthorityEntity.setAuthority(Authority.read);
        AuthorityEntity writeAuthorityEntity = new AuthorityEntity();
        writeAuthorityEntity.setAuthority(Authority.write);

        userEntity.addAuthorities(readAuthorityEntity, writeAuthorityEntity);
        UUID savedUserId = userRepository.save(userEntity).getId();
        String savedUsername = userRepository.save(userEntity).getUsername();
        kafkaTemplate.send("rangiffler_user", new UserEvent(savedUserId, savedUsername));
        LOG.info("### Kafka topic [rangiffler_user] sent message: {}", savedUsername);
        return savedUsername;
    }
}
