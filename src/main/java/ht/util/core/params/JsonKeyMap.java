package ht.util.core.params;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.GenericKeyValue;


public class JsonKeyMap extends GenericKeyValue<String, JsonNode> {
    public JsonKeyMap(final String s, final JsonNode treeMap) {
        super(s, treeMap);
    }
}