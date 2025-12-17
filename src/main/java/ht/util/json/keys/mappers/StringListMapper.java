package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.StringUtil;

import java.util.List;

public class StringListMapper implements Mapper<JsonNode, List<String>> {
    private String seperator;

    public StringListMapper(String seperator) {
        this.seperator = seperator;
    }

    public List<String> apply(JsonNode jsonNodes) {
        String vals = jsonNodes.textValue();
        if (vals != null) {
            String res[] = StringUtil.tokenizeFromSingleChar(vals, this.seperator, true);
            if (res != null) {
                return StringUtil.toList(res);
            }
        }
        return null;
    }
}
