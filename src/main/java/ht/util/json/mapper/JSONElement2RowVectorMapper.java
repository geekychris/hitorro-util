package ht.util.json.mapper;

import ht.util.core.iterator.mappers.Object2RowVectorMapper;
import ht.util.json.JSONElement;
import ht.util.json.JSONType;

/**
 * Map fields of a json object to a vector of strings. Can be used for such things as CSV writing
 */
public class JSONElement2RowVectorMapper extends Object2RowVectorMapper<JSONElement> {
    public JSONElement2RowVectorMapper(String[][] tuples) {
        super(tuples);
    }

    public JSONElement2RowVectorMapper(String[] keys, String[] targets) {
        super(keys, targets);
    }

    public Object get(String field, JSONElement e) {
        JSONElement elem = e.getFromPath(field);
        if (elem.getJSONType() == JSONType.Null) {
            return null;
        }
        return elem.toString();
    }
}
