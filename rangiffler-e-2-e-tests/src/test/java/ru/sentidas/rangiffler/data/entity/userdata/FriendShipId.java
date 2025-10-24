package ru.sentidas.rangiffler.data.entity.userdata;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class FriendShipId implements Serializable {

  private UUID requester;
  private UUID addressee;

}
