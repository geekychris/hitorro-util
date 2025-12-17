package ht.jsontypesystem.dynamic.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import ht.util.core.iterator.mappers.JsonInitableMapper;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseJsonInteropMapper extends JsonInitableMapper<JsonNode, JsonNode> {
    public String toStringFromJsonNode(JsonNode node) {
        return node.textValue();
    }

    public String stringMap(String node) {
        return null;
    }

    public long string2longMap(String s) {
        return 0;
    }

    public JsonNode toJsonNodeFromString(String s) {
        return JsonNodeFactory.instance.textNode(s);
    }

    public ArrayNode string2JsonVec(List<String> l) {
        ArrayNode an = JsonNodeFactory.instance.arrayNode();
        for (String s : l) {
            an.add(toJsonNodeFromString(s));
        }
        return an;
    }

    public List<String> json2StringVec(ArrayNode an) {
        ArrayList<String> l = new ArrayList<>();
        for (JsonNode e : an) {
            l.add(stringMap(e.textValue()));
        }
        return l;
    }


    public JsonNode applyString2String(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isArray()) {
            return string2JsonVec(json2StringVec((ArrayNode) jsonNode));
        } else {
            return toJsonNodeFromString(toStringFromJsonNode(jsonNode));
        }
    }


    public JsonNode applyString2long(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isArray()) {
            ArrayNode an = JsonNodeFactory.instance.arrayNode();
            for (JsonNode elem : jsonNode) {
                an.add(string2longMap(toStringFromJsonNode(elem)));
            }
            return an;
        } else {
            return JsonNodeFactory.instance.numberNode(string2longMap(toStringFromJsonNode(jsonNode)));
        }
    }
}
