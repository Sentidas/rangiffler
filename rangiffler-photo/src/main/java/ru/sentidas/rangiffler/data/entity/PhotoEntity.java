package ru.sentidas.rangiffler.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sentidas.rangiffler.model.StorageType;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "photo")
public class PhotoEntity {

    @Id
    @org.hibernate.annotations.UuidGenerator
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.BINARY)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID user;


    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage", nullable = false)
    private StorageType storage = StorageType.OBJECT;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo; // для режима BLOB

    @Column(name = "photo_url", length = 512)
    private String photoUrl; // для режима OBJECT (ключ MinIO или http)

    @Column(name = "photo_mime", nullable = false)
    private String photoMime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;
}