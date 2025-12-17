package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.json.keys.propaccess.Propaccess;

public class PropaccesspMap implements Mapper<JsonNode, Propaccess> {
    public static PropaccesspMap instance = new PropaccesspMap();

    public Propaccess apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isTextual()) {
            return new Propaccess(jsonNodes.asText());
        }
        return new Propaccess(jsonNodes.textValue());
    }
}
