package ht.util.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

public class JsonNodeTransEnum {

    public JsonNode string2Json(String s) {
        return JsonNodeFactory.instance.textNode(s);
    }


    public String string2Json(JsonNode s) {
        return s.textValue();
    }

}
