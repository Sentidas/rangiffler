package ru.sentidas.rangiffler.model;

import java.util.ArrayList;
import java.util.List;

public record TestData(String password,
                       List<Photo> photos,
                       List<User> friends,
                       List<User> incomeInvitations,
                       List<User> outcomeInvitations
) {

    public TestData(String password) {
        this(password, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
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

    private List<String> extractUsernames(List<User> users) {
        return users.stream().map(User::username).toList();
    }

    public TestData withUpdatedPhotos(List<Photo> updatedPhotos) {
        return new TestData(
                password,
                photos,
                friends,
                incomeInvitations,
                outcomeInvitations
        );
    }
}
