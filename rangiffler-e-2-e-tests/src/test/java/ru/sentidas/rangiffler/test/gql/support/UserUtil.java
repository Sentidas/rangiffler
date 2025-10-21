package ru.sentidas.rangiffler.test.gql.support;

import ru.sentidas.GetPeopleQuery;

import java.util.Objects;

public class UserUtil {

    public static GetPeopleQuery.Node findNodeById(GetPeopleQuery.Data page, String userId) {
        return page.users.edges.stream()
                .map(e -> e.node)
                .filter(n -> Objects.equals(n.id, userId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("node not found: " + userId));
    }


}
