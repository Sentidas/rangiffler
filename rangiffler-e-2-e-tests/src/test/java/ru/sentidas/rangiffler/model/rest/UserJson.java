package ru.sentidas.rangiffler.model.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sentidas.rangiffler.data.entity.userdata.FriendshipStatus;
import ru.sentidas.rangiffler.data.entity.userdata.UserEntity;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.model.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserJson(
    @JsonProperty("id")
    UUID id,
    @JsonProperty("username")
    String username,
    @JsonProperty("firstname")
    String firstname,
    @JsonProperty("surname")
    String surname,
    @JsonProperty("avatar")
    String avatar,
    @JsonProperty("avatarSmall")
    String avatarSmall,
    @JsonProperty("friendshipStatus")
    FriendshipStatus friendshipStatus,
    String countryCode,
    @JsonIgnore
    TestData testData) {

  public static UserJson fromEntity(UserEntity entity, FriendshipStatus friendshipStatus) {
    return new UserJson(
        entity.getId(),
        entity.getUsername(),
        entity.getFirstname(),
        entity.getSurname(),
        entity.getAvatar() != null && entity.getAvatar().length > 0 ? new String(entity.getAvatar(), StandardCharsets.UTF_8) : null,
        entity.getAvatarSmall() != null && entity.getAvatarSmall().length > 0 ? new String(entity.getAvatarSmall(), StandardCharsets.UTF_8) : null,
        friendshipStatus,
        entity.getCountryCode(),
        new TestData(
            null,
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()

        )
    );
  }

  public UserJson(String username) {
    this(username, null);
  }

  public UserJson(String username, TestData testData) {
    this(null, username, null, null, null, null, null, null, testData);
  }

  public UserJson withPassword(String password) {
    return withTestData(
        new TestData(
            password,
            testData.photos(),
            testData.friends(),
            testData.outcomeInvitations(),
            testData.incomeInvitations()
        )
    );
  }

  public UserJson withUsers(List<Photo> photos,
                            List<User> friends,
                            List<User> outcomeInvitations,
                            List<User> incomeInvitations) {
    return withTestData(
        new TestData(
            testData.password(),
                photos,
            friends,
            outcomeInvitations,
            incomeInvitations
        )
    );
  }

  public UserJson withTestData(TestData testData) {
    return new UserJson(
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
