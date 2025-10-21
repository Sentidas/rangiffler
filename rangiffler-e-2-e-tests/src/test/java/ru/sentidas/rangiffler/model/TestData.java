package ru.sentidas.rangiffler.model;

import java.util.ArrayList;
import java.util.List;

public record TestData(String password,
                       List<AppPhoto> photos,
                       List<AppUser> friends,
                       int friendsPhotosTotal,
                       List<AppUser> incomeInvitations,
                       List<AppUser> outcomeInvitations
) {

    public TestData(String password) {
        this(password, new ArrayList<>(), new ArrayList<>(), 0, new ArrayList<>(), new ArrayList<>());
    }

    public List<String> friendsUsernames() {
        return extractUsernames(friends);
    }

    public List<String> incomeInvitationsUsernames() {
        return extractUsernames(incomeInvitations);
    }

    public List<String> outcomeInvitationsUsernames() {
        return extractUsernames(outcomeInvitations);
    }

    private List<String> extractUsernames(List<AppUser> users) {
        return users.stream().map(AppUser::username).toList();
    }

    public TestData withUpdatedPhotos(List<AppPhoto> updatedphotos) {
        return new TestData(
                password,
                updatedphotos,
                friends,
                0,
                incomeInvitations,
                outcomeInvitations
        );
    }

    public TestData withFriendsPhotosTotal(int total) {
        return new TestData(
                password,
                photos,
                friends,
                total,
                incomeInvitations,
                outcomeInvitations
        );
    }
}
