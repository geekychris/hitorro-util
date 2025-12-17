package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public class JsonNodeToString implements Mapper<JsonNode, String> {
    public static JsonNodeToString instance = new JsonNodeToString();

    public String apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isTextual()) {
            return jsonNodes.asText();
        }
        return jsonNodes.textValue();
    }
}
