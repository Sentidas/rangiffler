package ru.sentidas.rangiffler.data.entity.geo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "country")
public class CountryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column
    private String code;

    @Column
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] flag;
}
