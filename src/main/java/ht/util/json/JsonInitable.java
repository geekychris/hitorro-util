package ht.util.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by chris on 3/6/16.
 */
public interface JsonInitable {
    boolean init(JsonNode node);

    default JsonInitable consume(String path, JsonNode node) {
        init(node.get(path));
        return this;
    }
}
