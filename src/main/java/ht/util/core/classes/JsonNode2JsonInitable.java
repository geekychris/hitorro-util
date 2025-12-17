package ht.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.JsonInitable;

public class JsonNode2JsonInitable<T extends JsonInitable> extends JsonNodeClassMapper<T> {
    public JsonNode2JsonInitable(final Class requiredSuper, final String key, Class defaultClass) {
        super(requiredSuper, key, defaultClass);
    }

    public T apply(final JsonNode s) {
        if (s == null) {
            return null;
        }
        T t = super.apply(s);
        if (t == null) {
            return null;
        }
        t.init(s);
        return t;
    }
}
