package ht.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.EnumContext;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.StringUtil;

public class EnumPropertyMapper<E extends Enum> implements Mapper<JsonNode, E> {
    public static JsonNodeToFile instance = new JsonNodeToFile();

    private EnumContext<E> ec;

    public EnumPropertyMapper(EnumContext<E> ec) {
        this.ec = ec;
    }

    public E apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        String v = jsonNodes.asText();
        if (StringUtil.nullOrEmptyString(v)) {
            return null;
        }
        return ec.getByShortName(v);
    }
}
