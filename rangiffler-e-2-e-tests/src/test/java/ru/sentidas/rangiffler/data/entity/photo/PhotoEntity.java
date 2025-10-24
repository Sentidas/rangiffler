package ru.sentidas.rangiffler.data.entity.photo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sentidas.rangiffler.model.AppPhoto;

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


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;


    public static PhotoEntity from(AppPhoto photo) {
        PhotoEntity entity = new PhotoEntity();
        entity.setId(photo.id());
        entity.setUser(photo.userId());
        entity.setCountryCode(photo.countryCode());
        entity.setDescription(photo.description());

        entity.setCreatedDate(photo.creationDate() != null
                ? new Date(photo.creationDate().getTime())
                : new Date());

        String source = photo.src();
        if (source != null && !source.isBlank() && source.startsWith("data:")) {
            ru.sentidas.rangiffler.DataUrl parsedDataUrl = ru.sentidas.rangiffler.DataUrl.parse(source);
            entity.setStorage(StorageType.BLOB);
            entity.setPhoto(parsedDataUrl.bytes());
            entity.setPhotoUrl(null);
        }
        return entity;
    }

}