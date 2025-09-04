package ru.sentidas.rangiffler.model;

import jakarta.annotation.Nonnull;

public record ErrorJson(@Nonnull String type,
                        @Nonnull String title,
                        int status,
                        @Nonnull String detail,
                        @Nonnull String instance) {

}
