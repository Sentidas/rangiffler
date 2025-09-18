package ru.sentidas.rangiffler.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonNodeAttributeConverter implements AttributeConverter<JsonNode, String> {
    private static final ObjectMapper M = new ObjectMapper();
    @Override public String convertToDatabaseColumn(JsonNode attribute) {
        try { return attribute == null ? "{}" : M.writeValueAsString(attribute); }
        catch (Exception e) { throw new IllegalArgumentException("JSON write failed", e); }
    }
    @Override public JsonNode convertToEntityAttribute(String dbData) {
        try { return dbData == null ? M.nullNode() : M.readTree(dbData); }
        catch (Exception e) { throw new IllegalArgumentException("JSON read failed", e); }
    }
}
