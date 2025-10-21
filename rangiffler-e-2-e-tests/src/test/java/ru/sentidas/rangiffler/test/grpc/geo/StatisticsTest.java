package ru.sentidas.rangiffler.test.grpc.geo;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL;
import static ru.sentidas.rangiffler.utils.ImageDataUrl.DATA_URL2;

@GrpcTest
@DisplayName("Grpc_Geo: statistics")
public class StatisticsTest extends BaseTest {

    private static final String ISO_CODE = "^[a-z]{2}$";

    @Test
    @DisplayName("Статистика: поля ответа страны корректны —  у пользователя есть фото")
    @User(photo = 1)
    void statisticsReturnsValidCountryFieldsWhenUserHasPhotos(AppUser user) {
        final Map<String, Integer> expected = expectedMyCountsCountryCode(user);
        StatResponse response = awaitMyStat(user, expected);

        assertCountyCodes(expected, response);

        assertAll("Country response must be valid",
                () -> response.getStatList().forEach(s -> {
                    CountryResponse c = s.getCountry();
                    assertAll(
                            () -> assertNotNull(c.getCode(), "country.code must not be null"),
                            () -> assertFalse(c.getCode().isBlank(), "country.code must not be blank"),
                            () -> assertTrue(c.getCode().matches(ISO_CODE),
                                    "code must be ISO alpha-2 in lower case, e.g. 'fr'"),
                            () -> assertNotNull(c.getName(), "country.name must not be null"),
                            () -> assertFalse(c.getName().isBlank(), "country.name must not be blank"),
                            () -> assertNotNull(c.getFlag(), "country.flag must not be null"),
                            () -> assertFalse(c.getFlag().isBlank(), "country.flag must not be blank"),
                            () -> assertTrue(c.getFlag().startsWith("data:image"),
                                    "country.flag should be a dataURL icon")
                    );
                })
        );
    }

    @Test
    @DisplayName("Статистика: ответ пустой  —  у пользователя нет фото")
    @User
    void statisticsReturnsValidCountryFieldsWhenAbsentPhoto(AppUser user) {
        StatResponse response = awaitMyStat(user, Map.of());
        assertAll("Empty stats expected for user without photos",
                () -> assertNotNull(response, "Response must not be null"),
                () -> assertEquals(0, response.getStatCount(), "Stat list size must be 0"),
                () -> assertTrue(response.getStatList().isEmpty(), "Stat list must be empty"),
                () -> assertTrue(counts(response).isEmpty(), "Country counts map must be empty")
        );
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "fr", count = 3),
            @Photo(countryCode = "cn", count = 1)
    })
    @DisplayName("Статистика (мои фото): корректно считается по моим фото")
    void statsMyPhotosCalculatedCorrectlyFromOwnPhotos(AppUser user) {
        final Map<String, Integer> expected = expectedMyCountsCountryCode(user);
        StatResponse actualRes = awaitMyStat(user, expected);

        assertAll("Statistics must equal aggregation of user's photos",
                () -> assertEquals(expected.size(), actualRes.getStatCount()),
                () -> assertCountyCodes(expected, actualRes)
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "fr", count = 2)
    })
    @DisplayName("Статистика (мои фото): увеличивается при создании фото в новой стране")
    void statsMyPhotosIncreaseWhenCreatePhotoInNewCountry(AppUser user) {
        final Map<String, Integer> expectedBefore = expectedMyCountsCountryCode(user);
        StatResponse before = awaitMyStat(user, expectedBefore);

        assertAll("Before creating a photo",
                () -> assertEquals(expectedBefore.size(), before.getStatCount(), "Country list size should match"),
                () -> assertCountyCodes(expectedBefore, before)
        );

        photoBlockingStub.createPhoto(CreatePhotoRequest.newBuilder()
                .setUserId(user.id().toString())
                .setCountryCode("cn")
                .setSrc(DATA_URL)
                .build());

        final Map<String, Integer> expectedAfter = withAdded(expectedBefore, "cn");
        StatResponse after = awaitMyStat(user, expectedAfter);

        assertAll("After creating a photo",
                () -> assertEquals(expectedAfter.size(), after.getStatCount()),
                () -> assertCountyCodes(expectedAfter, after)
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "fr", count = 2)
    })
    @DisplayName("Статистика (мои фото): увеличивается при создании фото в той же стране")
    void statsMyPhotosIncreaseWhenCreatePhotoInSameCountry(AppUser user) {
        final Map<String, Integer> expectedBefore = expectedMyCountsCountryCode(user);
        StatResponse before = awaitMyStat(user, expectedBefore);

        assertAll("Before creating a photo",
                () -> assertEquals(expectedBefore.size(), before.getStatCount(), "Country list size should match"),
                () -> assertCountyCodes(expectedBefore, before)
        );

        photoBlockingStub.createPhoto(CreatePhotoRequest.newBuilder()
                .setUserId(user.id().toString())
                .setCountryCode("fr")
                .setSrc(DATA_URL)
                .build());

        final Map<String, Integer> expectedAfter = withAdded(expectedBefore, "fr");
        StatResponse after = awaitMyStat(user, expectedAfter);

        assertAll("After creating a photo",
                () -> assertEquals(expectedAfter.size(), after.getStatCount()),
                () -> assertCountyCodes(expectedAfter, after)
        );
    }


    @Test
    @User(photos = {
            @Photo(countryCode = "cn", count = 2)
    })
    @DisplayName("Статистика (мои фото): уменьшается при удалении фото")
    void statsMyPhotosDecreaseWhenDeletePhoto(AppUser user) {
        final UUID userId = user.id();
        final UUID photoId1 = firstPhotoId(user, userId);
        final UUID photoId2 = photosIds(user, userId).get(1);

        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse response1 = awaitMyStat(user, before);
        assertAll("Before delete",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        photoBlockingStub.deletePhoto(DeletePhotoRequest.newBuilder()
                .setId(photoId1.toString())
                .setRequesterId(userId.toString())
                .build());

        final Map<String, Integer> afterOne = withRemoved(before, "cn");
        StatResponse response2 = awaitMyStat(user, afterOne);
        assertAll("After first delete",
                () -> assertEquals(afterOne.size(), response2.getStatCount()),
                () -> assertCountyCodes(afterOne, response2)
        );

        photoBlockingStub.deletePhoto(DeletePhotoRequest.newBuilder()
                .setId(photoId2.toString())
                .setRequesterId(userId.toString())
                .build());

        StatResponse response3 = awaitMyStat(user, Map.of());
        assertAll("After second delete",
                () -> assertEquals(0, response3.getStatCount()),
                () -> assertCountyCodes(Map.of(), response3)
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "cn", count = 1)
    })
    @DisplayName("Статистика (мои фото): пересчитывается при изменении countryCode у фото")
    void statsMyPhotosRecomputedWhenChangeCountryCode(AppUser user) {
        final UUID userId = user.id();
        final String photoId = firstPhotoId(user, userId).toString();

        final Map<String, Integer> expectedBefore = expectedMyCountsCountryCode(user);
        StatResponse before = awaitMyStat(user, expectedBefore);

        assertAll("Before changing countryCode",
                () -> assertEquals(expectedBefore.size(), before.getStatCount()),
                () -> assertCountyCodes(expectedBefore, before)
        );

        photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                .setPhotoId(photoId)
                .setRequesterId(userId.toString())
                .setCountryCode("fr")
                .build());

        final Map<String, Integer> expectedAfter = withDeltas(expectedBefore, Map.of("cn", -1, "fr", 1));
        StatResponse after = awaitMyStat(user, expectedAfter);

        assertAll("After changing countryCode from 'cn' to 'fr'",
                () -> assertEquals(expectedAfter.size(), after.getStatCount()),
                () -> assertCountyCodes(expectedAfter, after)
        );
    }

    @Test
    @User(photos = {
            @Photo(countryCode = "cn", description = "я тут", count = 1)
    })
    @DisplayName("Статистика (мои фото): не меняется при обновлении только описания")
    void statsMyPhotosUnchangedWhenUpdateDescriptionOnly(AppUser user) {
        final UUID userId = user.id();
        final UUID photoId = firstPhotoId(user, userId);

        final Map<String, Integer> expectedBefore = expectedMyCountsCountryCode(user);
        StatResponse before = awaitMyStat(user, expectedBefore);

        assertAll("Before updating description",
                () -> assertEquals(expectedBefore.size(), before.getStatCount(), "Country list size should match"),
                () -> assertCountyCodes(expectedBefore, before)
        );

        photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                .setPhotoId(photoId.toString())
                .setRequesterId(userId.toString())
                .setDescription("я там")
                .build());

        StatResponse after = awaitMyStat(user, expectedBefore);

        assertAll("After updating description only",
                () -> assertEquals(expectedBefore.size(), after.getStatCount(), "Country list size should match"),
                () -> assertCountyCodes(expectedBefore, after)
        );
    }

    @Test
    @DisplayName("Статистика (мои фото): не меняется — когда обновляем src как dataURL")
    @User(photos = {@Photo(countryCode = "fr", count = 1)})
    void statsMyPhotosUnchangedWhenUpdateSrcOnly(AppUser user) {
        final UUID uid = user.id();
        final UUID pid = firstPhotoId(user, uid);

        final Map<String, Integer> expected = expectedMyCountsCountryCode(user);
        StatResponse before = awaitMyStat(user, expected);
        assertAll("Before update src dataURL",
                () -> assertEquals(expected.size(), before.getStatCount()),
                () -> assertCountyCodes(expected, before)
        );

        photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                .setPhotoId(pid.toString())
                .setRequesterId(uid.toString())
                .setSrc(DATA_URL2)
                .build());

        StatResponse after = awaitMyStat(user, expected);
        assertAll("After update src dataURL",
                () -> assertEquals(expected.size(), after.getStatCount()),
                () -> assertCountyCodes(expected, after)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): корректно агрегируется — когда у меня и друзей есть фото")
    @User(friends = 2,
            photos = {
                    @Photo(countryCode = "fr", count = 3),
                    @Photo(countryCode = "cn", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cn", count = 2),
                    @Photo(owner = Photo.Owner.FRIEND, partyIndex = 1, countryCode = "fr", count = 2)
            }
    )
    void statsMyPlusFriendsAggregatedCorrectly(AppUser user) {
        final Map<String, Integer> expectedMine = expectedMyCountsCountryCode(user);
        StatResponse mine = awaitMyStat(user, expectedMine);

        assertAll("Own photos only",
                () -> assertEquals(expectedMine.size(), mine.getStatCount()),
                () -> assertCountyCodes(expectedMine, mine)
        );

        final Map<String, Integer> expectedFeed = expectedFeedCountsCountryCode(user);
        StatResponse feed = awaitFriendsStat(user, expectedFeed);

        assertAll("Own photos plus friends",
                () -> assertEquals(expectedFeed.size(), feed.getStatCount()),
                () -> assertCountyCodes(expectedFeed, feed)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): пересчитывается — когда друг меняет страну у фото")
    @User(photos = {
            @Photo(countryCode = "cn", count = 1),
            @Photo(owner = Photo.Owner.FRIEND, partyIndex = 0, countryCode = "cn", count = 1),
    }, friends = 1)
    void friendsStatsRecomputedWhenFriendChangesCountry(AppUser user) {
        final UUID fid = friendId(user, 0);
        final UUID pid = firstPhotoId(user, fid);

        final Map<String, Integer> before = expectedFeedCountsCountryCode(user);
        StatResponse response1 = awaitFriendsStat(user, before);
        assertAll("Before friend change country",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                .setPhotoId(pid.toString())
                .setRequesterId(fid.toString())
                .setCountryCode("fr")
                .build());

        final Map<String, Integer> expectedAfter = withDeltas(before, Map.of("cn", -1, "fr", 1));
        StatResponse response2 = awaitFriendsStat(user, expectedAfter);
        assertAll("After friend change country",
                () -> assertEquals(expectedAfter.size(), response2.getStatCount()),
                () -> assertCountyCodes(expectedAfter, response2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): растёт после принятия другом моего исходящего приглашения")
    @User(outcomeInvitation = 1,
            photos = {
                    @Photo(countryCode = "fr", count = 2),
                    @Photo(owner = Photo.Owner.OUTCOME, partyIndex = 0, countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.OUTCOME, partyIndex = 0, countryCode = "cn", count = 1),
            })
    void friendsAggregateIncreasesWhenOutcomeAccepted(AppUser user) {
        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse response1 = awaitFriendsStat(user, before);
        assertAll("Before accept friendship (outcome pending)",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        final Map<String, Integer> beforeWithFriend = expectedFeedCountsCountryCode(user);
        StatResponse withFriendRes = awaitFriendsStat(user, beforeWithFriend);
        assertAll("Before accept friendship (outcome pending)",
                () -> assertEquals(beforeWithFriend.size(), withFriendRes.getStatCount()),
                () -> assertCountyCodes(beforeWithFriend, withFriendRes)
        );

        final String invitee = user.testData().outcomeInvitations().getFirst().username();
        userdataBlockingStub.acceptFriendshipRequest(FriendshipRequest.newBuilder()
                .setUsername(invitee)
                .setUser(user.id().toString())
                .build());

        final Map<String, Integer> after = expectedFeedCountsCountryCode(user);
        final Map<String, Integer> afterWithNewFriend = withDeltas(after, Map.of("fr", 1, "cn", 1));

        StatResponse response2 = awaitFriendsStat(user, afterWithNewFriend);
        assertAll("After accept friendship (outcome accepted)",
                () -> assertEquals(afterWithNewFriend.size(), response2.getStatCount()),
                () -> assertCountyCodes(afterWithNewFriend, response2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): растёт после принятия мной приглашения от друга")
    @User(incomeInvitation = 1,
            photos = {
                    @Photo(countryCode = "fr", count = 2),
                    @Photo(owner = Photo.Owner.INCOME, partyIndex = 0, countryCode = "fr", count = 1),
                    @Photo(owner = Photo.Owner.INCOME, partyIndex = 0, countryCode = "cn", count = 1),
            })
    void friendsAggregateIncreasesWhenIncomeAccepted(AppUser user) {
        final UUID inviteeId = user.testData().incomeInvitations().getFirst().id();

        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse response1 = awaitFriendsStat(user, before);

        assertAll("Before accept friendship (income pending)",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        final Map<String, Integer> beforeWithFriend = expectedFeedCountsCountryCode(user);
        StatResponse withFriendRes = awaitFriendsStat(user, beforeWithFriend);
        assertAll("Before accept friendship (income pending)",
                () -> assertEquals(beforeWithFriend.size(), withFriendRes.getStatCount()),
                () -> assertCountyCodes(beforeWithFriend, withFriendRes)
        );

        userdataBlockingStub.acceptFriendshipRequest(FriendshipRequest.newBuilder()
                .setUsername(user.username())
                .setUser(inviteeId.toString())
                .build());

        final Map<String, Integer> after = expectedFeedCountsCountryCode(user);
        final Map<String, Integer> afterWithNewFriend = withDeltas(after, Map.of("fr", 1, "cn", 1));

        StatResponse response2 = awaitFriendsStat(user, afterWithNewFriend);
        assertAll("After accept friendship (income accepted)",
                () -> assertEquals(afterWithNewFriend.size(), response2.getStatCount()),
                () -> assertCountyCodes(afterWithNewFriend, response2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): уменьшается — когда удаляем друга")
    @User(photos = {@Photo(countryCode = "fr", count = 1)},
            friends = 1, friendsWithPhotosEach = 1)
    void friendsDecreasesWhenRemoveFriend(AppUser user) {
        final Map<String, Integer> before = expectedFeedCountsCountryCode(user);
        StatResponse response1 = awaitFriendsStat(user, before);
        assertAll("Before remove friend",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        AppUser friend = user.testData().friends().getFirst();
        userdataBlockingStub.removeFriend(FriendshipRequest.newBuilder()
                .setUsername(user.username())
                .setUser(friend.id().toString())
                .build());

        final Map<String, Integer> after = expectedMyCountsCountryCode(user);
        StatResponse response2 = awaitFriendsStat(user, after);

        assertAll("After remove friend",
                () -> assertEquals(after.size(), response2.getStatCount()),
                () -> assertCountyCodes(after, response2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): без эффекта — когда мной отклоняется входящее приглашение")
    @User(photos = {@Photo(countryCode = "fr", count = 1)},
            incomeInvitation = 1)
    void friendsNoEffectWhenDeclineIncomeInvitation(AppUser user) {
        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse response1 = awaitFriendsStat(user, before);
        assertAll("Before decline incoming",
                () -> assertEquals(before.size(), response1.getStatCount()),
                () -> assertCountyCodes(before, response1)
        );

        final AppUser inviter = user.testData().incomeInvitations().getFirst();
        userdataBlockingStub.declineFriendshipRequest(FriendshipRequest.newBuilder()
                .setUsername(user.username())
                .setUser(inviter.id().toString())
                .build());

        StatResponse response2 = awaitFriendsStat(user, before);
        assertAll("After decline incoming",
                () -> assertEquals(before.size(), response2.getStatCount()),
                () -> assertCountyCodes(before, response2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): без эффекта — когда адресат отклоняет исходящее приглашение")
    @User(photos = {@Photo(countryCode = "fr", count = 1)},
            outcomeInvitation = 1)
    void friendsNoEffectWhenInviteeDeclinesOutcome(AppUser user) {
        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse r1 = awaitFriendsStat(user, before);
        assertAll("Before invitee decline",
                () -> assertEquals(before.size(), r1.getStatCount()),
                () -> assertCountyCodes(before, r1)
        );

        final String invitee = user.testData().outcomeInvitations().getFirst().username();
        userdataBlockingStub.declineFriendshipRequest(FriendshipRequest.newBuilder()
                .setUsername(invitee)
                .setUser(user.id().toString())
                .build());

        StatResponse r2 = awaitFriendsStat(user, before);
        assertAll("After invitee decline",
                () -> assertEquals(before.size(), r2.getStatCount()),
                () -> assertCountyCodes(before, r2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): растёт только от принятой дружбы — когда входящих приглашений несколько")
    @User(photos = {@Photo(countryCode = "fr", count = 1)},
            incomeInvitation = 2)
    void friendsIncreasesOnlyFromAcceptedOneWhenMultipleIncome(AppUser user) {
        final Map<String, Integer> before = expectedMyCountsCountryCode(user);
        StatResponse r1 = awaitFriendsStat(user, before);
        assertAll("Before accept one of multiple",
                () -> assertEquals(before.size(), r1.getStatCount()),
                () -> assertCountyCodes(before, r1)
        );

        final AppUser inviter1 = user.testData().incomeInvitations().get(0);
        userdataBlockingStub.acceptFriendshipRequest(FriendshipRequest.newBuilder()
                .setUsername(user.username())
                .setUser(inviter1.id().toString())
                .build());

        final Map<String, Integer> after = expectedFeedCountsCountryCode(user);
        StatResponse r2 = awaitFriendsStat(user, after);
        assertAll("After accept one income",
                () -> assertEquals(after.size(), r2.getStatCount()),
                () -> assertCountyCodes(after, r2)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): без эффекта — когда у друга нет фото")
    @User(photos = {@Photo(countryCode = "fr", count = 1)},
            friends = 1, friendsWithPhotosEach = 0)
    void friendsNoEffectWhenFriendHasNoPhotos(AppUser user) {
        final Map<String, Integer> expected = expectedMyCountsCountryCode(user);
        StatResponse r = awaitFriendsStat(user, expected);
        assertAll("Friend without photos",
                () -> assertEquals(expected.size(), r.getStatCount()),
                () -> assertCountyCodes(expected, r)
        );
    }

    @Test
    @DisplayName("Статистика (я + друзья): без эффекта — когда друг обновляет только описание фото")
    @User(friends = 1, friendsWithPhotosEach = 1)
    void friendsNoEffectWhenFriendUpdatesDescription(AppUser user) {
        final Map<String, Integer> before = expectedFeedCountsCountryCode(user);
        StatResponse r1 = awaitFriendsStat(user, before);
        assertAll("Before friend updates description",
                () -> assertEquals(before.size(), r1.getStatCount()),
                () -> assertCountyCodes(before, r1)
        );

        final UUID fid = friendId(user, 0);
        final UUID pid = firstPhotoId(user, fid);

        photoBlockingStub.updatePhoto(UpdatePhotoRequest.newBuilder()
                .setPhotoId(pid.toString())
                .setRequesterId(fid.toString())
                .setDescription("updated by friend")
                .build());

        StatResponse r2 = awaitFriendsStat(user, before);
        assertAll("After friend updates description",
                () -> assertEquals(before.size(), r2.getStatCount()),
                () -> assertCountyCodes(before, r2)
        );
    }

    // ==== Негативные сценарии ====

    @Test
    @DisplayName("Статистика_пользователь не существует: NOT_FOUND")
    void geoStatisticsReturnsNotFoundWhenUserMissing() {
        final String unknownUserId = UUID.randomUUID().toString();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class, () ->
                geoBlockingStub.statistics(StatRequest.newBuilder()
                        .setUserId(unknownUserId)
                        .build())
        );
        assertEquals(Status.NOT_FOUND.getCode(), ex.getStatus().getCode(), "status should be NOT_FOUND");
    }

    @Test
    @DisplayName("Статистика user_id пустой: INVALID_ARGUMENT")
    void geoStatisticsReturnsInvalidArgumentWhenUserIdIsEmpty() {
        StatRequest request = StatRequest.newBuilder()
                .setUserId("")
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> geoBlockingStub.statistics(request),
                "exception should be thrown");
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(),
                "status should be INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("Статистика user_id не UUID: INVALID_ARGUMENT")
    void geoStatisticsReturnsInvalidArgumentWhenUserIdIsNotUuid() {
        StatRequest request = StatRequest.newBuilder()
                .setUserId("not-a-uuid")
                .build();

        StatusRuntimeException ex = assertThrows(StatusRuntimeException.class,
                () -> geoBlockingStub.statistics(request),
                "exception should be thrown");
        assertEquals(Status.INVALID_ARGUMENT.getCode(), ex.getStatus().getCode(),
                "status should be INVALID_ARGUMENT");
    }

    // ==== Helpers ====

    private StatResponse awaitStat(AppUser user,
                                     boolean withFriends,
                                     Map<String, Integer> expected,
                                     Duration timeout) {
        long end = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < end) {
            StatResponse response = geoBlockingStub.statistics(StatRequest.newBuilder()
                    .setUserId(user.id().toString())
                    .setWithFriends(withFriends)
                    .build());

            Map<String, Integer> got = response.getStatList().stream()
                    .collect(Collectors.toMap(s -> s.getCountry().getCode(), Stat::getCount));

            if (got.equals(expected)) {
                return response;
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
            }
        }
        throw new AssertionError("Expected statistics: " + expected + " for " + timeout.toMillis() + " мс");
    }

    private StatResponse awaitMyStat(AppUser user, Map<String, Integer> expected) {
        return awaitStat(user, false, expected, Duration.ofSeconds(5));
    }

    private StatResponse awaitFriendsStat(AppUser user, Map<String, Integer> expected) {
        return awaitStat(user, true, expected, Duration.ofSeconds(5));
    }

    private StatResponse awaitStat(AppUser user, boolean withFriends, Map<String, Integer> expected) {
        return awaitStat(user, withFriends, expected, Duration.ofSeconds(5));
    }

    private Map<String, Integer> counts(StatResponse r) {
        return r.getStatList().stream()
                .collect(Collectors.toMap(s -> s.getCountry().getCode(), Stat::getCount));
    }

    private void assertCountyCodes(Map<String, Integer> expected, StatResponse response) {
        assertEquals(expected, counts(response), "Expected country codes and counts to match exactly");
    }

    // +1 к стране
    private Map<String, Integer> withAdded(Map<String, Integer> base, String code) {
        return withAdded(base, code, 1);
    }

    // +n к стране
    private Map<String, Integer> withAdded(Map<String, Integer> base, String code, int n) {
        Map<String, Integer> res = new HashMap<>(base);
        res.merge(code, n, Integer::sum);
        return Map.copyOf(res);
    }

    // −1 у страны, удаляем ключ если стало <= 0
    private Map<String, Integer> withRemoved(Map<String, Integer> base, String code) {
        return withRemoved(base, code, 1);
    }

    // −n у страны, удаляем ключ если стало <= 0
    private Map<String, Integer> withRemoved(Map<String, Integer> base, String code, int n) {
        Map<String, Integer> res = new HashMap<>(base);
        res.merge(code, -n, Integer::sum);
        res.entrySet().removeIf(e -> e.getValue() == null || e.getValue() <= 0);
        return Map.copyOf(res);
    }

    // Применить пакет дельт: положительные — добавить, отрицательные — вычесть
    private Map<String, Integer> withDeltas(Map<String, Integer> base, Map<String, Integer> deltas) {
        Map<String, Integer> res = new HashMap<>(base);
        deltas.forEach((code, d) -> res.merge(code, d, Integer::sum));
        res.entrySet().removeIf(e -> e.getValue() == null || e.getValue() <= 0);
        return Map.copyOf(res);
    }
}
