package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;

import java.util.Set;

public class ValidatedStringMapper implements Mapper<JsonNode, String> {

    private Set<String> validatedList;

    public ValidatedStringMapper(Set<String> validatedList) {
        this.validatedList = validatedList;
    }

    public String apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isTextual()) {
            String v = jsonNodes.asText();
            if (validatedList.contains(v)) {
                return v;
            }
            return null;
        }
        String v = jsonNodes.textValue();
        if (validatedList.contains(v)) {
            return v;
        }
        return null;
    }
}
