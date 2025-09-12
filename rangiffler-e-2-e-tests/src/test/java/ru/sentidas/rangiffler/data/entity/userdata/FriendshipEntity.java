package ru.sentidas.rangiffler.data.entity.userdata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "friendship")
@IdClass(FriendShipId.class)
public class FriendshipEntity {

  @Id
  @ManyToOne
  @JoinColumn(name = "requester_id", referencedColumnName = "id")
  private UserEntity requester;

  @Id
  @ManyToOne
  @JoinColumn(name = "addressee_id", referencedColumnName = "id")
  private UserEntity addressee;

  @Column(name = "created_date")
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdDate;

  @Column
  @Enumerated(EnumType.STRING)
  private FriendshipStatus status; // PENDING / ACCEPTED

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FriendshipEntity that)) return false;
    UUID r1 = requester != null ? requester.getId() : null;
    UUID a1 = addressee != null ? addressee.getId() : null;
    UUID r2 = that.requester != null ? that.requester.getId() : null;
    UUID a2 = that.addressee != null ? that.addressee.getId() : null;
    return Objects.equals(r1, r2) && Objects.equals(a1, a2);
  }

  @Override
  public int hashCode() {
    UUID r = requester != null ? requester.getId() : null;
    UUID a = addressee != null ? addressee.getId() : null;
    return Objects.hash(r, a);
  }
}
