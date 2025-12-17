package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public class JsonNodeToDouble implements Mapper<JsonNode, Double> {
    public static JsonNodeToDouble instance = new JsonNodeToDouble();

    public Double apply(JsonNode jsonNodes) {
        if (jsonNodes.isFloatingPointNumber()) {
            return jsonNodes.asDouble();
        }
        return Double.parseDouble(jsonNodes.asText());
    }
}