package ru.sentidas.rangiffler.rest.model.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record JwtToken(
        JwtHeader header,
        JwtPayload payload,
        String signature
) {
    private static final ObjectMapper M = new ObjectMapper();

    /**
     * Парсит строку JWT (с/без префикса "Bearer ") в модель JwtToken
     */
    public static JwtToken parse(String token) {
        final String pfx = "Bearer ";
        final String jwt = token != null && token.startsWith(pfx)
                ? token.substring(pfx.length())
                : token;

        // 2) Формат: должно быть 3 части
        final String[] parts = jwt.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("JWT должен состоять из 3 частей: header.payload.signature");
        }

        // 3) header/payload в base64url
        ObjectNode headerJson = readJsonUrlBase64(parts[0], "header");
        ObjectNode payloadJson = readJsonUrlBase64(parts[1], "payload");

        JwtHeader header = JwtHeader.from(headerJson);
        JwtPayload payload = JwtPayload.from(payloadJson);

        // 5) Подпись —  третья часть без декодирования (для прозрачноcти проверок)
        final String signature = parts[2];

        return new JwtToken(header, payload, signature);
    }

    // base64url decode -> JSON ObjectNode
    private static ObjectNode readJsonUrlBase64(String base64url, String sectionName) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(base64url);
            JsonNode node = M.readTree(new String(bytes, StandardCharsets.UTF_8));
            if (!node.isObject()) {
                throw new IllegalArgumentException(sectionName + " не является JSON-объектом");
            }
            return (ObjectNode) node;
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось распарсить " + sectionName + ": " + e.getMessage(), e);
        }
    }
}
