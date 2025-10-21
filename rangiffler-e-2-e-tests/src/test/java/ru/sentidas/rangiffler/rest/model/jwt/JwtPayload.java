package ru.sentidas.rangiffler.rest.model.jwt;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record JwtPayload(
        String issuer,          // "iss"  — издатель токена
        String subject,         // "sub"  — идентификатор пользователя - username
        String audience,        // "aud"  — клиент, для которого выдан токен
        String authorizedParty, // "azp"  — клиент, от имени которого запрошен токен
        long authTime,          // "auth_time" — момент аутентификации пользователя
        long issuedAt,          // "iat" — время выпуска токена
        long expiresAt,         // "exp" — время истечения срока действия
        String jwtId,           // "jti" — уникальный идентификатор токена
        String sessionId        // "sid" — идентификатор сессии
) {

    public static JwtPayload from(ObjectNode payload) {
        return new JwtPayload(
                payload.path("iss").asText(null),
                payload.path("sub").asText(null),
                payload.path("aud").asText(null),
                payload.path("azp").asText(null),
                payload.path("auth_time").asLong(0),
                payload.path("iat").asLong(0),
                payload.path("exp").asLong(0),
                payload.path("jti").asText(null),
                payload.path("sid").asText(null)
        );
    }
}
