package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.iterator.Mapper;

public class ResolvableJson2StringMapper implements Mapper<JsonNode, String> {
    public static ResolvableJson2StringMapper instance = new ResolvableJson2StringMapper();

    public String apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isTextual()) {
            return JVSProperties.resolveJsonVariable(jsonNodes.asText());
        }
        return JVSProperties.resolveJsonVariable(jsonNodes.textValue());
    }
}
