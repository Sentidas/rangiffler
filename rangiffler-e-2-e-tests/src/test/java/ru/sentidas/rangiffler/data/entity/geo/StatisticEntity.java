package ru.sentidas.rangiffler.data.entity.geo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "statistic")
public class StatisticEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;


    @Column(name = "country_id")
    private UUID countryId;

    @Column
    private int count;
}
