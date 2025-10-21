package ru.sentidas.rangiffler.test.gql.support;

import ru.sentidas.GetFeedQuery;
import java.util.Objects;
import java.util.UUID;

public final class PhotoUtil {

    private PhotoUtil() {}

    public static int likesTotal(GetFeedQuery.Node node) {
        return node.likes != null ? node.likes.total : 0;
    }

    public static boolean containsLikeFromUser(GetFeedQuery.Node node, UUID userId) {
        return node.likes != null && node.likes.likes.stream()
                .anyMatch(l -> Objects.equals(l.user, userId.toString()));
    }

    public static GetFeedQuery.Node findPhotoById(GetFeedQuery.Data feed, String id) {
        return feed.feed.photos.edges.stream()
                .map(e -> e.node)
                .filter(n -> Objects.equals(n.id, id))
                .findFirst()
                .orElseThrow(() -> new AssertionError("photo not found in feed"));
    }
}
