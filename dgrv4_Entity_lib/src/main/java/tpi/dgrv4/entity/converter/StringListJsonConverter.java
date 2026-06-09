package tpi.dgrv4.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> LIST_OF_STRING = new TypeReference<>() {
    };
    private static final String EMPTY_JSON_ARRAY = "[]";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null) {
            return EMPTY_JSON_ARRAY;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert List<String> to JSON string.", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(dbData, LIST_OF_STRING);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to convert JSON string to List<String>.", e);
        }
    }
}
