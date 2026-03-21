package com.framework.utils.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * JSON data utility using Jackson.
 * Reads test data from src/test/resources or file paths.
 * Supports POJO mapping, List, Map, and raw JSON.
 */
public final class JsonUtils {

    private static final Logger log = LogManager.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils() {}

    // ── Read ─────────────────────────────────────────────────────────────

    /**
     * Read JSON file from classpath and map to POJO.
     * Usage: JsonUtils.readFromClasspath("test-data/user.json", User.class)
     */
    public static <T> T readFromClasspath(String resourcePath, Class<T> clazz) {
        try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resourcePath);
            T result = MAPPER.readValue(is, clazz);
            log.debug("Loaded JSON as [{}] from: {}", clazz.getSimpleName(), resourcePath);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from classpath: " + resourcePath, e);
        }
    }

    /**
     * Read JSON file and map to a List of POJOs.
     * Usage: JsonUtils.readListFromClasspath("test-data/users.json", User.class)
     */
    public static <T> List<T> readListFromClasspath(String resourcePath, Class<T> clazz) {
        try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resourcePath);
            return MAPPER.readValue(is, MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON list from classpath: " + resourcePath, e);
        }
    }

    /**
     * Read JSON file from absolute file path.
     */
    public static <T> T readFromFile(String filePath, Class<T> clazz) {
        try {
            return MAPPER.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from file: " + filePath, e);
        }
    }

    /**
     * Read JSON as Map<String, Object> — useful for dynamic payloads.
     */
    public static Map<String, Object> readAsMap(String resourcePath) {
        return readFromClasspath(resourcePath, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Read JSON string directly into POJO.
     */
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialise JSON string", e);
        }
    }

    // ── Write ────────────────────────────────────────────────────────────

    public static String toJsonString(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialise object to JSON", e);
        }
    }

    public static void writeToFile(Object obj, String filePath) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), obj);
            log.info("Written JSON to: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON to file: " + filePath, e);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private static <T> T readFromClasspath(String resourcePath, TypeReference<T> typeRef) {
        try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("Resource not found: " + resourcePath);
            return MAPPER.readValue(is, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from classpath: " + resourcePath, e);
        }
    }
}
