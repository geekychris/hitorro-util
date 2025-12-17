package ht.util.typesystem.valuesource;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.json.keys.StringProperty;

/**
 * Field me, if defined for field x and x is null, will compute from wherever it was told to compute from.
 * <p/>
 * Must be re-entrant
 */
public abstract class ValueMapMapper {
    public static final StringProperty FieldKey = new StringProperty("srcfield", "", null);
    protected String fromField;
    protected JsonNode map;

    public boolean init(JsonNode map) {
        this.map = map;
        fromField = FieldKey.apply(map);
        return true;
    }

    public abstract boolean compute(String requestedField, ValueSourceForClass vsfc);

}
