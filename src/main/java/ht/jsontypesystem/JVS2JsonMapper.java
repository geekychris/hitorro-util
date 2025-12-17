package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.mappers.BaseMapper;

public class JVS2JsonMapper extends BaseMapper<JVS, JsonNode> {
    public static JVS2JsonMapper me = new JVS2JsonMapper();

    @Override
    public JsonNode apply(final JVS jvs) {
        return jvs.getJsonNode();
    }
}
