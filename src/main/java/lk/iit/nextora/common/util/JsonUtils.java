package lk.iit.nextora.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class for JSON operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * String json = JsonUtils.toJson(user);
 * User user = JsonUtils.fromJson(json, User.class);
 * Optional<String> value = JsonUtils.getFieldValue(json, "email");
 * </pre>
 */
@Slf4j
public final class JsonUtils {

    private JsonUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    private static final ObjectMapper objectMapper = createObjectMapper();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Get the shared ObjectMapper instance
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // ==================== Serialization ====================

    /**
     * Convert object to JSON string
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    /**
     * Convert object to JSON string (returns null on error instead of throwing)
     */
    public static String toJsonSafe(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Convert object to pretty-printed JSON string
     */
    public static String toPrettyJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", e.getMessage());
            throw new RuntimeException("JSON serialization failed", e);
        }
    }

    // ==================== Deserialization ====================

    /**
     * Convert JSON string to object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Convert JSON string to object (returns Optional.empty() on error)
     */
    public static <T> Optional<T> fromJsonSafe(String json, Class<T> clazz) {
        try {
            return Optional.ofNullable(objectMapper.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Convert JSON string to object with TypeReference (for generics)
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Convert JSON string to List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to List: {}", e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    /**
     * Convert JSON string to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> fromJsonToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to Map: {}", e.getMessage());
            throw new RuntimeException("JSON deserialization failed", e);
        }
    }

    // ==================== Object Conversion ====================

    /**
     * Convert object to another type
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        return objectMapper.convertValue(obj, clazz);
    }

    /**
     * Convert object to Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    // ==================== JSON Node Operations ====================

    /**
     * Parse JSON string to JsonNode
     */
    public static JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON: {}", e.getMessage());
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * Get field value from JSON string
     */
    public static Optional<String> getFieldValue(String json, String fieldName) {
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode != null && !fieldNode.isNull()
                    ? Optional.of(fieldNode.asText())
                    : Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to get field '{}' from JSON: {}", fieldName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get nested field value from JSON string (e.g., "user.address.city")
     */
    public static Optional<String> getNestedFieldValue(String json, String path) {
        try {
            JsonNode node = objectMapper.readTree(json);
            String[] parts = path.split("\\.");
            for (String part : parts) {
                if (node == null || node.isNull()) return Optional.empty();
                node = node.get(part);
            }
            return node != null && !node.isNull()
                    ? Optional.of(node.asText())
                    : Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Failed to get nested field '{}' from JSON: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    // ==================== Validation ====================

    /**
     * Check if string is valid JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isBlank()) return false;
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}

