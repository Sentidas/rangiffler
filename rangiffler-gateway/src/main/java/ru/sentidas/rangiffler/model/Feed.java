package ru.sentidas.rangiffler.model;

import org.springframework.data.domain.Slice;

import java.util.List;

public record Feed(
        String username,
        boolean withFriends,
        Slice<Photo> photos,
        List<Stat> stats
) {

}
