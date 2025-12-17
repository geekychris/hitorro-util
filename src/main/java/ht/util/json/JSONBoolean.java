package ht.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.json.visitors.JSONVisitor;

import java.io.IOException;

/**
 *
 */
public class JSONBoolean extends JSONElement<Boolean> {
    public static final JSONBoolean True = new JSONBoolean(true);
    public static final JSONBoolean False = new JSONBoolean(false);

    private Boolean s;

    public JSONBoolean(Boolean s) {
        this.s = s;
    }

    public void visit(JSONVisitor visitor, final int depth) {

    }

    @Override
    public Boolean get() {
        return s;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.Boolean;
    }

    @Override
    public int getAggregateSize() {
        return 10;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeBoolean(s);
    }

    public String toString() {
        return s.toString();
    }
}