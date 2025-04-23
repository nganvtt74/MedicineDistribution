package com.example.medicinedistribution.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Reads JSON from a resource file
     * @param resourcePath Path to the resource file
     * @return JsonNode containing the parsed JSON
     * @throws IOException if there's an error reading or parsing the file
     */
    public static JsonNode readJsonFromResource(String resourcePath) throws IOException {
        try (InputStream is = JsonUtil.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return mapper.readTree(is);
        }
    }

    /**
     * Reads JSON from a file
     * @param filePath Path to the JSON file
     * @return JsonNode containing the parsed JSON
     * @throws IOException if there's an error reading or parsing the file
     */
    public static JsonNode readJsonFromFile(String filePath) throws IOException {
        log.info("Reading JSON from file: {}", new File(filePath).getAbsolutePath());
        return mapper.readTree(new File(filePath));
    }

    /**
     * Writes JSON to a file
     * @param node JsonNode to write
     * @param filePath Path to the output file
     * @throws IOException if there's an error writing the file
     */
    public static void writeJsonToFile(JsonNode node, String filePath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), node);
    }

    /**
     * Saves a resource file to the file system (useful for extracting config files)
     * @param resourcePath Source path in resources
     * @param outputPath Destination path in file system
     * @throws IOException if there's an error copying the file
     */
    public static void saveResourceToFile(String resourcePath, String outputPath) throws IOException {
        try (InputStream is = JsonUtil.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            Path targetPath = Paths.get(outputPath);
            // Create parent directories if they don't exist
            Files.createDirectories(targetPath.getParent());

            // Copy the file
            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Converts a Java object to JsonNode
     * @param object Object to convert
     * @return JsonNode representation of the object
     * @throws JsonProcessingException if there's an error converting the object
     */
    public static JsonNode toJsonNode(Object object) throws JsonProcessingException {
        return mapper.valueToTree(object);
    }

    /**
     * Converts a JsonNode to a Java object
     * @param node JsonNode to convert
     * @param valueType Class of the target object
     * @return Converted Java object
     * @throws JsonProcessingException if there's an error converting the node
     */
    public static <T> T fromJsonNode(JsonNode node, Class<T> valueType) throws JsonProcessingException {
        return mapper.treeToValue(node, valueType);
    }

    /**
     * Creates a new empty JSON object
     * @return Empty ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    /**
     * Converts a string to a JsonNode
     * @param jsonString JSON string
     * @return JsonNode representation
     * @throws IOException if the string cannot be parsed as valid JSON
     */
    public static JsonNode parseString(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }

    /**
     * Converts a JsonNode to a pretty-printed string
     * @param node JsonNode to convert
     * @return Formatted JSON string
     */
    public static String prettyPrint(JsonNode node) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            log.error("Error formatting JSON", e);
            return node.toString();
        }
    }

    /**
     * Gets the ObjectMapper instance used by this utility
     * @return The ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static JsonObject parseJson(String jsonString, Class<JsonObject> jsonObjectClass) {
        try {
            return mapper.readValue(jsonString, jsonObjectClass);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON string: {}", e.getMessage());
            return null;
        }
    }
}