package ht.util.json.operators;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;
import ht.util.json.JSONElement;

/**
 *
 */
public class JSONFieldExists implements HTPredicate<JSONElement> {
    private String field;

    public JSONFieldExists(String field) {
        this.field = field;
    }

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final JSONElement jsonElement) {
        JSONElement elem = jsonElement.getFromPath(field);
        return elem != null;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }
}

