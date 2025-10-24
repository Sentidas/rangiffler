package ru.sentidas.rangiffler.test.kafka;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.KafkaTest;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.kafka.PhotoStatEvent;
import ru.sentidas.rangiffler.service.impl.KafkaService;
import ru.sentidas.rangiffler.service.impl.PhotoApiClient;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@KafkaTest
public class PhotoStatKafkaTest {

    private final PhotoApiClient photoClient = new PhotoApiClient();

    @Test
    @User(photo = 1)
    @DisplayName("Создание фото — приходит событие со счетчиком +1 по соответствующей стране")
    void createsPhotoSendPlusOneDeltaWhenNewPhotoAdded(AppUser user) throws Exception {
        UUID userId = user.id();
        AppPhoto photo = AnnotationHelper.firstPhoto(user, user.id());

        PhotoStatEvent photoFromKafka = KafkaService.getPhotoStat(userId, photo.countryCode());

        assertAll("photo create -> delta +1",
                () -> assertNotNull(photoFromKafka, "PhotoStatEvent must be received"),
                () -> assertEquals(userId, photoFromKafka.userId(), "userId must match"),
                () -> assertEquals(photo.countryCode(), photoFromKafka.countryCode(), "countryCode must match"),
                () -> assertEquals(1, photoFromKafka.delta(), "delta must be +1 on create")
        );
    }


    @Test
    @User(photos = { @Photo(countryCode = "it") })
    @DisplayName("Смена страны — приходит +1 для новой и -1 для старой (ожидаем по предикату)")
    void updatesPhotoSendsPlusOneForNewAndMinusOneForOld(AppUser user) throws Exception {
        UUID userId = user.id();
        AppPhoto photo = AnnotationHelper.firstPhoto(user, user.id());

        String oldCountry = photo.countryCode();   // "it"
        String newCountry = "cu";

        photoClient.updatePhoto(photo.id(), userId, newCountry);

        PhotoStatEvent plusNew  = KafkaService.waitPhotoStat(userId, newCountry, 5_000, e -> e.delta() == 1);
        PhotoStatEvent minusOld = KafkaService.waitPhotoStat(userId, oldCountry, 5_000, e -> e.delta() == -1);

        assertAll("country change -> +1 new / -1 old",
                () -> assertNotNull(plusNew,  "plusNew event must be received"),
                () -> assertEquals(1, plusNew.delta(), "delta must be +1 for new country"),
                () -> assertEquals(newCountry, plusNew.countryCode(), "countryCode must be NEW"),
                () -> assertEquals(userId, plusNew.userId(), "userId in +1 must match"),

                () -> assertNotNull(minusOld, "minusOld event must be received"),
                () -> assertEquals(-1, minusOld.delta(), "delta must be -1 for old country"),
                () -> assertEquals(oldCountry, minusOld.countryCode(), "countryCode must be OLD"),
                () -> assertEquals(userId, minusOld.userId(), "userId in -1 must match")
        );
    }

    @Test
    @User(photo = 1)
    @DisplayName("Удаление фото — приходит -1 по стране фото (ожидаем по предикату)")
    void deletesPhotoSendsMinusOneForCountry(AppUser user) throws Exception {
        UUID userId = user.id();
        AppPhoto photo = AnnotationHelper.firstPhoto(user, user.id());
        String country = photo.countryCode();

        // действие
        photoClient.deletePhoto(photo.id(), userId);

        // ждём нужную дельту для этой страны и пользователя
        PhotoStatEvent minus = KafkaService.waitPhotoStat(userId, country, 15_000, e -> e.delta() == -1);

        // проверки (soft-блок)
        assertAll("photo delete -> delta -1",
                () -> assertNotNull(minus, "event must be received"),
                () -> assertEquals(-1, minus.delta(), "delta must be -1 on delete"),
                () -> assertEquals(country, minus.countryCode(), "countryCode must match"),
                () -> assertEquals(userId, minus.userId(), "userId must match")
        );
    }
}
