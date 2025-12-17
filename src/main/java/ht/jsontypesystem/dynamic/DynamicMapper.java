package ht.jsontypesystem.dynamic;

//ht.jsontypesystem.dynamic.DynamicMapper

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.core.iterator.mappers.JsonInitableMapper;
import ht.util.json.keys.JsonInitableProperty;
import ht.util.json.keys.propaccess.Propaccess;

public class DynamicMapper extends DynamicFieldMapper {
    public static final JsonInitableProperty<JsonInitableMapper<JsonNode, JsonNode>> dynamicFieldMapperKey = new JsonInitableProperty("mapper", "", null, JsonInitableMapper.class, null);
    private JsonInitableMapper<JsonNode, JsonNode> mapper;
    @Override
    public boolean init(final JsonNode node) {
        super.init(node);
        mapper = dynamicFieldMapperKey.apply(node);
        return true;
    }

    public JsonNode map(JVS jvs, Propaccess pa, int depth) {
        JsonNode arr[] = getValues(jvs, pa, depth);
        if (arr.length > 0) {
            return mapper.apply(arr[0]);
        }
        return null;
    }
}
