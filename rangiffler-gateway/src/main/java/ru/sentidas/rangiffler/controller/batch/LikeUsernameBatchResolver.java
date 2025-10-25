package ru.sentidas.rangiffler.controller.batch;

import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.service.api.GrpcUserdataClient;

import java.util.*;

/**
 * Батч-резолвер username для Like: собираем уникальные userId,
 * одним bulk-запросом получаем usernames
 * и раскладываем их обратно по соответствующим
 * Like в рамках одного GraphQL-запроса.
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class LikeUsernameBatchResolver {

    private final GrpcUserdataClient grpcUserdataClient;

    public LikeUsernameBatchResolver(GrpcUserdataClient grpcUserdataClient) {
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @BatchMapping(typeName = "Like", field = "username")
    public Map<Like, String> resolveUsernames(List<Like> likes) {
        // 1) Уникальные userId c сохранением порядка
        LinkedHashSet<UUID> uniqueUserIds = likes.stream()
                .map(Like::user)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

        // 2) Один bulk в userdata → Map<userId, username>
        Map<UUID, String> usernamesById = grpcUserdataClient.getUsernamesByIds(List.copyOf(uniqueUserIds));

        // 3) Вернуть мапу "исходный Like-инстанс → username (или пустая строка, т.к. String!)"
        Map<Like, String> result = new LinkedHashMap<>(likes.size());
        for (Like like : likes) {
            UUID userId = like.user();
            String username = usernamesById.get(userId);
            result.put(like, username != null ? username : "");
        }
        return result;
    }
}
