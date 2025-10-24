package ru.sentidas.rangiffler.data.entity.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class PhotoLikeId implements Serializable {

    @Column(name = "photo_id", columnDefinition = "BINARY(16)")
    private UUID photoId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;
}
