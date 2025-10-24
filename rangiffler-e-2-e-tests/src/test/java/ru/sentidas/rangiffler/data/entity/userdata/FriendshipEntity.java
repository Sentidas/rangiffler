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
}
