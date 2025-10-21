package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.data.entity.userdata.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;

import java.util.ArrayList;
import java.util.UUID;

public record AppUser(
        UUID id,
        String username,
        String firstname,
        String surname,
        String avatar,
        String avatarSmall,
        FriendshipStatus friendshipStatus,
        String countryCode,
        TestData testData
) {

    public AppUser(String username, TestData testData) {
        this(null, username, null, null, null, null, null, null, testData);
    }

    public AppUser(String username, String countryCode, TestData testData) {
        this(null, username, null, null, null, null, null, countryCode, testData);
    }

    public static AppUser fromEntity(UserEntity entity, FriendshipStatus friendshipStatus) {

        return new AppUser(
                entity.getId(),
                entity.getUsername(),
                entity.getFirstname(),
                entity.getSurname(),
                null,
                null,
                friendshipStatus,
                entity.getCountryCode(),
                new TestData(
                        null,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        0,
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );
    }

    public AppUser withPassword(String password) {
        return withTestData(
                new TestData(
                        password,
                        testData.photos(),
                        testData.friends(),
                        testData.friendsPhotosTotal(),
                        testData.incomeInvitations(),
                        testData.outcomeInvitations()
                )
        );
    }

    public AppUser withTestData(TestData testData) {
        return new AppUser(
                id,
                username,
                firstname,
                surname,
                avatar,
                avatarSmall,
                friendshipStatus,
                countryCode,
                testData
        );
    }
}
