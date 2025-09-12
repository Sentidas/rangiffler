package ru.sentidas.rangiffler.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

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

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] avatar;

  @Lob
  @Column(name = "avatar_small", columnDefinition = "LONGBLOB")
  private byte[] avatarSmall;

  @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY)
  private List<FriendshipEntity> friendshipRequests = new ArrayList<>();

  @OneToMany(mappedBy = "addressee", fetch = FetchType.LAZY)
  private List<FriendshipEntity> friendshipAddressees = new ArrayList<>();

  @Column(name = "country_code", length = 2, nullable = false)
  private String countryCode;

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
//
//  public void addPhotos(PhotoEntity... photos) {
//    this.photos.addAll(Stream.of(photos).peek(
//        p -> p.setUser(this)
//    ).toList());
//  }
//
//  public void removeFriends(UserEntity... friends) {
//    List<UUID> idsToBeRemoved = Arrays.stream(friends).map(UserEntity::getId).toList();
//    for (Iterator<FriendshipEntity> i = getFriendshipRequests().iterator(); i.hasNext(); ) {
//      FriendshipEntity friendsEntity = i.next();
//      if (idsToBeRemoved.contains(friendsEntity.getAddressee().getId())) {
//        friendsEntity.setAddressee(null);
//        i.remove();
//      }
//    }
//  }
//
  public void removeInvites(UserEntity... invitations) {
    List<UUID> idsToBeRemoved = Arrays.stream(invitations).map(UserEntity::getId).toList();
    for (Iterator<FriendshipEntity> i = getFriendshipAddressees().iterator(); i.hasNext(); ) {
      FriendshipEntity friendsEntity = i.next();
      if (idsToBeRemoved.contains(friendsEntity.getRequester().getId())) {
        friendsEntity.setRequester(null);
        i.remove();
      }
    }
  }
//
//  public void removePhotos(PhotoEntity... photos) {
//    List<UUID> idsToBeRemoved = Arrays.stream(photos).map(PhotoEntity::getId).toList();
//    for (Iterator<PhotoEntity> i = getPhotos().iterator(); i.hasNext(); ) {
//      PhotoEntity photoEntity = i.next();
//      if (idsToBeRemoved.contains(photoEntity.getId())) {
//        photoEntity.setUser(null);
//        i.remove();
//      }
//    }
//  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    UserEntity that = (UserEntity) o;
    return getId() != null && Objects.equals(getId(), that.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
  }
}
