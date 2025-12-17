package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public class JsonNodeToLong implements Mapper<JsonNode, Long> {
    public static JsonNodeToLong instance = new JsonNodeToLong();

    public Long apply(JsonNode jsonNodes) {
        if (jsonNodes.isLong()) {
            return jsonNodes.asLong();
        }
        return Long.parseLong(jsonNodes.asText());
    }
}
