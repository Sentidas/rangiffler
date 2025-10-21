package ru.sentidas.rangiffler.model;

import java.util.List;

public record UsersPage(
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        int page,
        int size,
        List<AppUser> content
) {}