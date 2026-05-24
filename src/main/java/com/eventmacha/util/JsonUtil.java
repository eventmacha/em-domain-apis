package com.eventmacha.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Thread-safe JSON serialization / deserialization helper.
 * Injected as a CDI singleton to avoid repeated ObjectMapper construction.
 */
@Singleton
public class JsonUtil {

    private static final Logger LOG = Logger.getLogger(JsonUtil.class);

    private final ObjectMapper mapper;

    public JsonUtil() {
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Serialise an object to a JSON string.
     * Returns {@code null} on failure (logged as error).
     */
    public String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to serialise object to JSON: %s", obj.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Deserialise a JSON string to the given class.
     */
    public <T> Optional<T> fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to deserialise JSON to %s", clazz.getSimpleName());
            return Optional.empty();
        }
    }

    /**
     * Deserialise a JSON string using a TypeReference (e.g. generic collections).
     */
    public <T> Optional<T> fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(json, typeRef));
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to deserialise JSON via TypeReference");
            return Optional.empty();
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
