package ru.sentidas.rangiffler.data.entity.photo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sentidas.rangiffler.model.Photo;

import java.util.Base64;
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

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] photo;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date", nullable = false)
    private Date createdDate;

//    // Односторонняя связь на лайки: FK в таблице photo_like (колонка photo_id)
//    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "photo_id", referencedColumnName = "id")
//    private Set<LikeEntity> likes = new HashSet<>();

    public static PhotoEntity fromJson(Photo photo) {
        PhotoEntity fe = new PhotoEntity();
        fe.setId(photo.id());
        fe.setUser(photo.userId());
        fe.setCountryCode(photo.countryCode());
        fe.setCreatedDate(new Date(photo.creationDate().getTime()));
        fe.setDescription(photo.description());
        // 🔽 Конвертируем base64 → byte[]
        if (photo.src() != null && photo.src().startsWith("data:image")) {
            String base64 = photo.src().substring(photo.src().indexOf(",") + 1);
            fe.setPhoto(Base64.getDecoder().decode(base64));
        } else {
            fe.setPhoto(null);
        }
        return fe;
    }

}