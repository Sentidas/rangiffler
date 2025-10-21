package ru.sentidas.rangiffler.utils;

import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.CountryName;
import ru.sentidas.rangiffler.model.TestData;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class AnnotationHelper {

    @Nonnull
    public static String getUniqueTestUsername() {
        return "testUser_" + System.currentTimeMillis() / 1000;
    }

    @Nonnull
    public static List<UUID> friendIds(AppUser user) {
        return user.testData().friends().stream()
                .map(AppUser::id)
                .toList();
    }

    @Nonnull
    public static List<String> friendIdsToString(AppUser user) {
        return user.testData().friends().stream()
                .map(f -> f.id().toString())
                .toList();
    }

    @Nonnull
    public static UUID firstFriendId(AppUser user) {
        return user.testData().friends().getFirst().id();
    }

    @Nonnull
    public static UUID firstFullFriendId(AppUser user) {
        return user.testData().friends().stream()
                .filter(f ->
                        f.firstname() != null && !f.firstname().isBlank())
                .findFirst().orElseThrow().id();
    }

    @Nonnull
    public static UUID firstEmptyFriendId(AppUser user) {
        return user.testData().friends().stream()
                .filter(f ->
                        f.firstname() == null)
                .findFirst().orElseThrow().id();
    }

    @Nonnull
    public static UUID firstOutcomeUserId(AppUser user) {
        return user.testData().outcomeInvitations().stream()
                .findFirst().orElseThrow().id();
    }

    @Nonnull
    public static UUID firstIncomeUserId(AppUser user) {
        return user.testData().incomeInvitations().stream()
                .findFirst().orElseThrow().id();
    }

    @Nonnull
    public static AppUser dataUserById(AppUser user, String findId) {
        TestData td = user.testData();
        List<AppUser> friends = td.friends() != null ? td.friends() : List.of();
        List<AppUser> outcome = td.outcomeInvitations() != null ? td.outcomeInvitations() : List.of();
        List<AppUser> income = td.incomeInvitations() != null ? td.incomeInvitations() : List.of();

        return Stream.concat(
                        friends.stream(),
                        Stream.concat(income.stream(), outcome.stream())
                )
                .filter(u -> u.id().toString().equals(findId))
                .findFirst().orElseThrow(() ->
                        new NoSuchElementException("User " + findId + " not found in friends/income/outcome"));
    }

    @Nonnull
    public static String friendIdtoString(AppUser user) {
        return user.testData().friends().getFirst().id().toString();
    }

    @Nonnull
    public static String friendUsername(AppUser user) {
        return user.testData().friends().getFirst().username();
    }

    @Nonnull
    public static List<UUID> myPhotoIds(AppUser user) {
        return user.testData().photos().stream()
                .map(ru.sentidas.rangiffler.model.AppPhoto::id)
                .toList();
    }

    @Nonnull
    public static List<String> myPhotoIdsToString(AppUser user) {
        return user.testData().photos().stream()
                .map(p -> p.id().toString())
                .collect(Collectors.toList());
    }

    @Nonnull
    public static List<String> friendsPhotoIds(AppUser user) {
        return user.testData().friends().stream()
                .flatMap(f -> f.testData().photos().stream())
                .map(p -> p.id().toString())
                .toList();
    }

    @Nonnull
    public static UUID firstFriendId(AppUser user, int friendIndex) {
        return user.testData().friends().get(friendIndex).id();
    }

    @Nonnull
    public static String countryByCode(AppUser user, int indexPhoto) {
        return CountryName.labelByCode(user.testData().photos().get(indexPhoto).countryCode());
    }

    @Nonnull
    public static String countryName(String code) {
        return CountryName.labelByCode(code);
    }

    @Nonnull
    public static String photoDescription(AppUser user, int indexPhoto) {
        return user.testData().photos().get(indexPhoto).description();
    }

    @Nonnull
    public static List<UUID> photosIds(AppUser user, UUID ownerId) {

        if (ownerId.equals(user.id())) {
            return user.testData().photos().stream()
                    .map(AppPhoto::id)
                    .toList();
        }
        return user.testData().friends().stream()
                .filter(f -> f.id().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("owner (friend) not found: " + ownerId))
                .testData().photos().stream()
                .map(AppPhoto::id)
                .toList();

    }

    @Nonnull
    public static List<AppPhoto> photos(AppUser user, UUID ownerId) {

        if (ownerId.equals(user.id())) {
            return user.testData().photos();
        }
        return user.testData().friends().stream()
                .filter(f -> f.id().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("owner (friend) not found: " + ownerId))
                .testData().photos();

    }

    @Nonnull
    public static AppPhoto firstPhoto(AppUser user, UUID ownerId) {

        if (ownerId.equals(user.id())) {
            return user.testData().photos().getFirst();
        }
        return user.testData().friends().stream()
                .filter(f -> f.id().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("owner (friend) not found: " + ownerId))
                .testData().photos().getFirst();

    }

    @Nonnull
    public static UUID firstMyPhotoId(AppUser user) {
        return user.testData().photos().getFirst().id();

    }

    @Nonnull
    public static UUID firstPhotoId(AppUser user, UUID ownerId) {

        if (ownerId.equals(user.id())) {
            return user.testData().photos().stream()
                    .findFirst()
                    .map(AppPhoto::id)
                    .orElseThrow(() -> new IllegalStateException("no photos for owner: " + ownerId));


        }
        return user.testData().friends().stream()
                .filter(f -> f.id().equals(ownerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("owner (friend) not found: " + ownerId))
                .testData().photos().stream()
                .findFirst()
                .map(AppPhoto::id)
                .orElseThrow(() -> new IllegalStateException("no photos for owner: " + ownerId));
    }

    @Nonnull
    public static String firstFriendUsername(AppUser user) {
        return user.testData().friends().getFirst().username();
    }

    @Nonnull
    public static UUID incomingInviterId(AppUser user) {
        return user.testData().incomeInvitations().getFirst().id();
    }

    @Nonnull
    public static String incomingInviterIdToString(AppUser user) {
        return user.testData().incomeInvitations().getFirst().id().toString();
    }

    @Nonnull
    public static String incomingInviterUsername(AppUser user) {
        return user.testData().incomeInvitations().getFirst().username();
    }

    @Nonnull
    public static UUID outgoingInviteeId(AppUser user) {
        return user.testData().outcomeInvitations().getFirst().id();
    }


    @Nonnull
    public static String outgoingInviteeIdToString(AppUser user) {
        return user.testData().outcomeInvitations().getFirst().id().toString();
    }

    @Nonnull
    public static String outgoingInviteeUsername(AppUser user) {
        return user.testData().outcomeInvitations().getFirst().username();
    }

    @Nonnull
    public static AppPhoto myPhoto(AppUser user, int photoIndex) {
        List<AppPhoto> own = user.testData().photos();
        if (photoIndex < 0 || photoIndex >= own.size()) {
            throw new IndexOutOfBoundsException("Own photo index out of bounds: " + photoIndex + " (total: " + own.size() + ")");
        }
        return own.get(photoIndex);
    }

    @Nonnull
    public static String myPhotoCountryLabel(AppUser user, int photoIndex) {
        AppPhoto p = myPhoto(user, photoIndex);
        return CountryName.labelByCode(p.countryCode());
    }

    @Nonnull
    public static String myPhotoDescription(AppUser user, int photoIndex) {
        return myPhoto(user, photoIndex).description();
    }

    @Nonnull
    public static AppPhoto friendPhoto(AppUser user, int friendIndex, int photoIndex) {
        List<AppUser> friends = user.testData().friends();
        if (friendIndex < 0 || friendIndex >= friends.size()) {
            throw new IndexOutOfBoundsException("Friend index out of bounds: " + friendIndex + " (total: " + friends.size() + ")");
        }
        List<AppPhoto> photos = friends.get(friendIndex).testData().photos();
        if (photoIndex < 0 || photoIndex >= photos.size()) {
            throw new IndexOutOfBoundsException("Friend's photo index out of bounds: " + photoIndex + " (total: " + photos.size() + ")");
        }
        return photos.get(photoIndex);
    }


    @Nonnull
    public static String friendPhotoCountryLabel(AppUser user, int friendIndex, int photoIndex) {
        AppPhoto p = friendPhoto(user, friendIndex, photoIndex);
        return CountryName.labelByCode(p.countryCode());
    }


    @Nonnull
    public static String friendPhotoDescription(AppUser user, int friendIndex, int photoIndex) {
        return friendPhoto(user, friendIndex, photoIndex).description();
    }

    @Nonnull
    public static UUID friendId(AppUser user, int friendIndex) {
        return user.testData().friends().get(friendIndex).id();
    }


    // ожидание только по "моим фото"
    public static Map<String, Integer> expectedMyCountsCountryCode(AppUser user) {
        return user.testData().photos().stream()
                .collect(Collectors.groupingBy(AppPhoto::countryCode, Collectors.summingInt(p -> 1)));
    }

    // ожидание по "я + друзья"
    public static Map<String, Integer> expectedFeedCountsCountryCode(AppUser user) {
        Map<String, Integer> mine = expectedMyCountsCountryCode(user);
        Map<String, Integer> friends = user.testData().friends().stream()
                .flatMap(f -> f.testData().photos().stream())
                .collect(Collectors.groupingBy(AppPhoto::countryCode, Collectors.summingInt(p -> 1)));

        friends.forEach((code, c) -> mine.merge(code, c, Integer::sum));
        return mine;
    }

}
