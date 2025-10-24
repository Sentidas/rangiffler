package ru.sentidas.rangiffler.test.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.KafkaTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.kafka.PhotoStatEvent;
import ru.sentidas.rangiffler.model.kafka.UserEvent;
import ru.sentidas.rangiffler.rest.AuthApi;
import ru.sentidas.rangiffler.rest.core.RestClient;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.service.impl.KafkaService;
import ru.sentidas.rangiffler.service.impl.PhotoApiClient;
import ru.sentidas.rangiffler.service.impl.UsersDbClient;
import ru.sentidas.rangiffler.utils.AnnotationHelper;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;

import static org.junit.jupiter.api.Assertions.assertEquals;

@KafkaTest
public class AuthKafkaTest {

    private static final Config CFG = Config.getInstance();

    private final AuthApi authApi = new RestClient.EmtyRestClient(CFG.authUrl()).create(AuthApi.class);
    private final UsersDbClient dbClient = new UsersDbClient();
    private final PhotoApiClient apiClient = new PhotoApiClient();
    @Test
    void userShouldBeProducedToKafka() throws Exception {
        final String username = "kafka_111" + GenerationDataUser.randomUsername();
        final String password = "12345";

        authApi.requestRegisterForm().execute();
        authApi.register(
                username,
                password,
                password,
                ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
        ).execute();

        UserEvent userFromKafka = KafkaService.getUser(username);
        assertEquals(
                username,
                userFromKafka.username()
        );
    }


    @Test
    @User(photo = 1)
    @DisplayName("Создание фото — приходит событие со счетчиком +1 по соответствующей стране")
    void userShouldBeProducedToKafka4(AppUser user) throws Exception {
        AppPhoto photo = AnnotationHelper.firstPhoto(user, user.id());

        PhotoStatEvent photoFromKafka = KafkaService.getPhotoStat(user.id(), photo.countryCode());

        assertEquals(photo.countryCode(), photoFromKafka.countryCode());
        assertEquals(user.id(), photoFromKafka.userId());
        assertEquals(1, photoFromKafka.delta());
    }
}
