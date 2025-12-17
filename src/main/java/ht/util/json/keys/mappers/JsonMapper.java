package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.function.Function;

public class JsonMapper implements Function<JsonNode, JsonNode> {
    private Propaccess access;

    public JsonMapper(String path) {
        access = new Propaccess(path);
    }

    public JsonMapper(Propaccess path) {
        access = path;
    }

    @Override
    public JsonNode apply(final JsonNode jsonNode) {
        // identity mapping function
        return jsonNode;
    }
}
