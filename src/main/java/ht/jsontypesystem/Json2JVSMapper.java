package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.mappers.BaseMapper;

public class Json2JVSMapper extends BaseMapper<JsonNode, JVS> {
    public static Json2JVSMapper me = new Json2JVSMapper();

    @Override
    public JVS apply(final JsonNode jsonNode) {
        return new JVS(jsonNode);
    }
}
