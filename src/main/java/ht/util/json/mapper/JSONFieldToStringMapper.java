package ht.util.json.mapper;

import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.JSONElement;
import ht.util.json.JSONType;

/**
 *
 */
public class JSONFieldToStringMapper extends BaseMapper<JSONElement, String> {
    private String field;

    public JSONFieldToStringMapper(String field) {
        this.field = field;
    }

    @Override
    public String apply(final JSONElement e) {
        JSONElement o = e.getFromPath(field);
        if (o == null || o.getJSONType() == JSONType.Null) {
            return null;
        }
        return o.toString();
    }
}
