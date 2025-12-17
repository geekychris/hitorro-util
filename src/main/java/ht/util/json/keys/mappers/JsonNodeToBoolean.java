package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.BooleanUtil;
import ht.util.core.iterator.Mapper;

/**
 *
 */
public class JsonNodeToBoolean implements Mapper<JsonNode, Boolean> {
    public static JsonNodeToBoolean instance = new JsonNodeToBoolean();

    public Boolean apply(JsonNode jsonNodes) {
        if (jsonNodes.isBoolean()) {
            return jsonNodes.asBoolean();
        }
        return BooleanUtil.getBoolean(jsonNodes.asText());
    }
}
