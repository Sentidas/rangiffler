package ru.sentidas.rangiffler.data.entity.userdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sentidas.rangiffler.model.AppUser;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@Entity
@Table(name = "`user`")
public class UserEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column
    private String firstname;

    @Column
    private String surname;

    @Column(name = "country_code", length = 2, nullable = false)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage", nullable = false)
    private StorageType storage = StorageType.BLOB;

    @Column(name = "object_key")
    private String objectKey;        // OBJECT-режим

    @Lob
    @Column(name = "avatar", columnDefinition = "LONGBLOB")
    private byte[] avatar;           // BLOB-режим: оригинал

    @Column(name = "mime")
    private String mime;             // MIME оригинала (опционально)

    @Lob
    @Column(name = "avatar_small", columnDefinition = "LONGBLOB")
    private byte[] avatarSmall;      // PNG превью


    @OneToMany(
            mappedBy = "requester",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FriendshipEntity> friendshipRequests = new ArrayList<>();

    @OneToMany(
            mappedBy = "addressee",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FriendshipEntity> friendshipAddressees = new ArrayList<>();

    public void addFriends(FriendshipStatus status, UserEntity... friends) {
        List<FriendshipEntity> friendsEntities = Stream.of(friends)
                .map(f -> {
                    FriendshipEntity fe = new FriendshipEntity();
                    fe.setRequester(this);
                    fe.setAddressee(f);
                    fe.setStatus(status);
                    fe.setCreatedDate(new Date());
                    return fe;
                }).toList();
        this.friendshipRequests.addAll(friendsEntities);
    }


    public static UserEntity from(AppUser user) {
        UserEntity ue = new UserEntity();
        ue.setUsername(user.username());
        ue.setFirstname(user.firstname());
        ue.setSurname(user.surname());
        ue.setCountryCode(user.countryCode());

        // BLOB: кладём байты, если пришёл dataURL
        if (user.avatar() != null && user.avatar().startsWith("data:image")) {
            String base64 = user.avatar().substring(user.avatar().indexOf(',') + 1);
            ue.setAvatar(Base64.getDecoder().decode(base64));
        } else {
            ue.setAvatar(null);
        }
        if (user.avatarSmall() != null && user.avatarSmall().startsWith("data:image")) {
            String base64s = user.avatarSmall().substring(user.avatarSmall().indexOf(',') + 1);
            ue.setAvatarSmall(Base64.getDecoder().decode(base64s));
        } else {
            ue.setAvatarSmall(null);
        }

        return ue;
    }
}
