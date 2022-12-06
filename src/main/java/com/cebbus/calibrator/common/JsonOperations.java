package com.cebbus.calibrator.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component
public class JsonOperations {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        MAPPER.registerModule(new JavaTimeModule());
    }

    public static <T> List<T> stringToList(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }

        CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);

        try {
            return MAPPER.readValue(json, collectionType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static <K, V> Map<K, V> stringToMap(String json, Class<K> key, Class<V> value) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }

        MapType mapType = MAPPER.getTypeFactory().constructMapType(HashMap.class, key, value);

        try {
            return MAPPER.readValue(json, mapType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    public static <T> T stringToObj(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String objToJsonString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void objToJsonFile(File file, Object value) {
        try {
            MAPPER.writeValue(file, value);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static <T> List<T> fileToObjList(File file, Class<T> clazz) {
        CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);

        try {
            return MAPPER.readValue(file, collectionType);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return Collections.emptyList();
    }

    public static ObjectNode objToObjectNode(Object value) {
        return stringToObjectNode(objToJsonString(value));
    }

    public static ObjectNode stringToObjectNode(String value) {
        try {
            JsonNode jsonNode = MAPPER.readTree(value);
            if (jsonNode.isObject()) {
                return jsonNode.deepCopy();
            }

            throw new RuntimeException("Parameter is not an object.");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Value("${spring.jackson.date-format}")
    public void setDateFormatPattern(String dateFormatPattern) {
        MAPPER.setDateFormat(new SimpleDateFormat(dateFormatPattern));
    }
}
