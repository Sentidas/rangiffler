package ru.sentidas.rangiffler.test.gql.support;

import com.apollographql.apollo.api.ApolloResponse;

import java.util.List;
import java.util.Map;

public final class ErrorGql {

    private ErrorGql() {
    }

    public static String message(ApolloResponse<?> response) {
        if (response.errors == null) {
            return null;
        }
        return response.errors.getFirst().getMessage();
    }

    public static String classification(ApolloResponse<?> response) {
        Map<String, Object> extensions = response.errors.getFirst().getExtensions();
        return extensions != null ? (String) extensions.get("classification") : null;
    }

    public static String error(ApolloResponse<?> response) {
        Map<String, Object> extensions = response.errors.getFirst().getExtensions();
        return extensions != null ? (String) extensions.get("error") : null;
    }

    public static String path(ApolloResponse<?> response) {
        if (response.errors == null || response.errors.isEmpty()) {
            return null;
        }

        List<Object> path = response.errors.getFirst().getPath();
        if (path == null || path.isEmpty()) {
            return null;
        }
        return (String) path.getFirst();
    }
}

