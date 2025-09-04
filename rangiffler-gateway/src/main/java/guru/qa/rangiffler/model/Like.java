package guru.qa.rangiffler.model;

import guru.qa.rangiffler.entity.LikeEntity;

import java.util.Date;
import java.util.UUID;

public record Like (
        UUID user,
        String username,
        Date creationDate

) {
        public static Like fromEntity(LikeEntity likeEntity) {
            return new Like(
                    likeEntity.getUser().getId(),
                    likeEntity.getUser().getUsername(),
                    likeEntity.getCreationDate()
            );
        }
}
