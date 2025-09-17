package ru.sentidas.rangiffler.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "photo_like")
public class LikeEntity {

    @EmbeddedId
    private PhotoLikeId id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date creationDate;
}
