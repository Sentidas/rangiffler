package guru.qa.rangiffler.model;

import java.util.List;

public record Likes(
        int total,
        List<Like> likes
) {
}
