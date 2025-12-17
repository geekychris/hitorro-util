package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.mappers.JsonMapper;
import ht.util.json.keys.propaccess.Propaccess;

public class JsonProperty extends BaseMappingProperty<JsonNode> {
    public JsonProperty(String path, String description, JsonNode defaultValue) {
        super(new Propaccess(path), description, defaultValue, new JsonMapper(path));
    }

    public JsonProperty(Propaccess path, String description, JsonNode defaultValue) {
        super(path, description, defaultValue, new JsonMapper(path));
    }
}

