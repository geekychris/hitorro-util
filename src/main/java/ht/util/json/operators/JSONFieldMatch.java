package ht.util.json.operators;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;
import ht.util.json.JSONElement;
import ht.util.json.JSONType;

/**
 *
 */
public class JSONFieldMatch implements HTPredicate<JSONElement> {
    private String field;
    private String value;

    public JSONFieldMatch(String field, String value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final JSONElement jsonElement) {
        JSONElement elem = jsonElement.getFromPath(field);
        if (elem == null) {
            return false;
        }
        if (elem.getJSONType() != JSONType.String) {
            return false;
        }
        return value.equals(elem.toString());
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }
}
