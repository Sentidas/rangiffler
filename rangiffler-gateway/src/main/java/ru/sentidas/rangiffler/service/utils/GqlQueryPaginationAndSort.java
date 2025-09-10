package ru.sentidas.rangiffler.service.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nonnull;

public class GqlQueryPaginationAndSort {
    private final int page;
    private final int size;

    public GqlQueryPaginationAndSort(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public @Nonnull Pageable pageable() {
        return PageRequest.of(
                page,
                size
        );
    }
}
