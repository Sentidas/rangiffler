package ru.sentidas.rangiffler.test.fake;

import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.service.PhotoDbClient;

import java.util.Date;
import java.util.UUID;

public class PhotoTest {

    @Test
    void createPhoto() {
        PhotoDbClient spendDbClient = new PhotoDbClient();
        UUID requesterId = UUID.fromString("c2baacc6-2409-47d3-83ec-5748b3a91f38");  // duck

        Photo spend = spendDbClient.createPhoto(
                new Photo(
                        null,
                        requesterId,
                        "c2baacc6-2409-47d3-83ec-5748b3a91f38",
                        "RU",
                        "тест",
                        new Date(),
                        0
                )
        );
        System.out.println(spend);
    }


    @Test
    void updatePhoto() {
        PhotoDbClient spendDbClient = new PhotoDbClient();
        UUID requesterId = UUID.fromString("651ddf97-8c00-4a03-878c-898e27bb0876");   // fox
        UUID photoId = UUID.fromString("96dc9dd8-e596-4af3-b976-38af3e6a3d47");

        Photo photo = spendDbClient.updatePhoto(
                new Photo(
                        photoId,
                        requesterId,
                        "c2baacc6-2409-47d3-83ec-5748b3a91f38",
                        "RU",
                        "my summer",
                        new Date(),
                        0
                )
        );
        System.out.println(photo);
    }

    @Test
    void deletePhoto() {
        PhotoDbClient photoDbClient = new PhotoDbClient();
        UUID photoId = UUID.fromString("a9c0c765-58b1-4c79-a322-6153285b0aab");
        UUID requesterId = UUID.fromString("651ddf97-8c00-4a03-878c-898e27bb0876"); // fox
        Photo photo =
                new Photo(
                        photoId,
                        requesterId,
                        null,
                        null,
                        null,
                        null,
                        0
                );

        photoDbClient.removePhoto(photo);
    }


//    @Test
//    void findPhotoById() {
//        UUID requesterId = UUID.fromString("651ddf97-8c00-4a03-878c-898e27bb0876");
//        UUID photoId = UUID.fromString("96dc9dd8-e596-4af3-b976-38af3e6a3d47");
//        String username = "duck";
//
//        PhotoDbClient photoDbClient = new PhotoDbClient();
//        Optional<Photo> photo = photoDbClient.(spendId, username);
//
//        photo.ifPresentOrElse(
//                spendJson -> {
//                    System.out.println("Spend найден: " + photo.get().description());
//                    System.out.println("Spend категории: " + photo.get().category().name());
//                },
//                () -> System.out.println("Spend с таким Id не найден"));
//    }
//
//    @Test
//    void findSpendByUsernameAndDescription() {
//        PhotoDbClient spendDbClient = new PhotoDbClient();
//        Optional<SpendJson> photo = spendDbClient.findByUsernameAndDescription("duck", "Школа_62");
//
//        photo.ifPresentOrElse(
//                spendJson -> {
//                    System.out.println("Spend найден: " + photo.get().description());
//                    System.out.println("Spend категории: " + photo.get().category().name());
//                },
//                () -> System.out.println("Spend с таким Id не найден"));
//    }
//
}



