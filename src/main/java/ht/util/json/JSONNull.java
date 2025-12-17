package ht.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.json.visitors.JSONVisitor;

import java.io.IOException;

/**
 *
 */
public class JSONNull extends JSONElement<Object> {
    public static final JSONNull me = new JSONNull();

    @Override
    public Object get() {
        return this;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.Null;
    }

    @Override
    public void visit(final JSONVisitor visitor, final int depth) {
    }

    public int getAggregateSize() {
        return 8;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeNull();
    }
}
