package ru.sentidas.rangiffler.utils.generator;

import com.github.javafaker.Faker;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class RandomDataUtils {

    private static final Faker faker = new Faker();
    private static final Random RANDOM = new Random();


    public static String randomUsername() {
        String username;
        do {
            username = faker.animal().name();
        } while (username.length() < 3);
        return username + new Random().nextInt(100);
    }

    public static String randomDataUser() {
        String username;
        do {
            username = faker.animal().name();
        } while (username.length() < 3);
        return username + new Random().nextInt(100);
    }

    public static String randomName() {
        return faker.name().firstName();
    }

    public static String randomSurname() {
        return faker.name().lastName();
    }

    public static String randomSentence(int wordsCount) {
        return String.join(" ", faker.lorem().words(wordsCount));
    }


    public static String randomAvatar() {
        int index = RANDOM.nextInt(10) + 1; // 1..10
        Path path = Paths.get("rangiffler-e-2-e-tests/src/test/resources/avatar/" + index + ".png");

        try {
            byte[] bytes = Files.readAllBytes(path);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении изображения: " + path, e);
        }
    }

    public static String randomPhoto() {
        int index = RANDOM.nextInt(26) + 1; // 1..10
        Path path = Paths.get("rangiffler-e-2-e-tests/src/test/resources/photo/" + index + ".png");

        try {
            byte[] bytes = Files.readAllBytes(path);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении фото: " + path, e);
        }
    }
}
