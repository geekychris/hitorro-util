package ht.util.json.mapper;

import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.JSONElement;
import ht.util.json.JSONMap;
import ht.util.json.JSONString;

/**
 * Decorate a json apply with the hitorro type string
 */
public class AddTypeToJSON extends BaseMapper<JSONElement, JSONElement> {
    public static final String HTTypeField = "ht_type";
    private JSONString type;

    public AddTypeToJSON(String type) {
        this.type = new JSONString(type);
    }

    @Override
    public JSONElement apply(final JSONElement e) {
        JSONMap map = (JSONMap) e;
        map.put(HTTypeField, type);
        return e;
    }
}
