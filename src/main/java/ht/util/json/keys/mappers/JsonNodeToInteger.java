package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public class JsonNodeToInteger implements Mapper<JsonNode, Integer> {
    public static JsonNodeToInteger instance = new JsonNodeToInteger();

    public Integer apply(JsonNode jsonNodes) {
        if (jsonNodes.isInt()) {
            return jsonNodes.asInt();
        }
        return Integer.parseInt(jsonNodes.textValue());
    }
}
