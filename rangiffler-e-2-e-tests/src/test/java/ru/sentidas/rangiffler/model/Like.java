package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.data.entity.photo.LikeEntity;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record Like(
        UUID photoId,
        UUID userId,
        Date creationDate

) {
    public static Like fromEntity(LikeEntity likeEntity) {
        return new Like(
                likeEntity.getId().getPhotoId(),
                likeEntity.getId().getUserId(),
                likeEntity.getCreationDate()
        );
    }

    // один лайк -> grpc.Like
    public ru.sentidas.rangiffler.grpc.Like toProto() {
        var b = ru.sentidas.rangiffler.grpc.Like.newBuilder()
                .setUserId(userId.toString());
        if (creationDate != null) {
            b.setCreationDate(
                    com.google.protobuf.util.Timestamps.fromMillis(creationDate.getTime())
            );
        }
        return b.build();
    }

    // список лайков -> grpc.Likes (total + likes[])
    public static ru.sentidas.rangiffler.grpc.Likes toProtoList(List<Like> likes) {
        var lb = ru.sentidas.rangiffler.grpc.Likes.newBuilder()
                .setTotal(likes.size());
        for (var l : likes) {
            lb.addLikes(l.toProto());
        }
        return lb.build();
    }
}
