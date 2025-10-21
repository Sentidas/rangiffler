package ru.sentidas.rangiffler.rest.model.jwt;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Назначение:
 *  Представляет заголовок JWT
 */
public record JwtHeader(
        String algorithm, // "alg", например: RS256
        String keyId      // "kid", например: c9228d4b-...
) {

    public static JwtHeader from(ObjectNode header) {
        return new JwtHeader(
                header.path("alg").asText(null),
                header.path("kid").asText(null)
        );
    }
}
