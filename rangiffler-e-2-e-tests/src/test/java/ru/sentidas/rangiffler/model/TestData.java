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
