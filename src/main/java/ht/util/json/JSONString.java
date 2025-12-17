package ht.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.json.visitors.JSONVisitor;

import java.io.IOException;


/**
 *
 */
public class JSONString extends JSONElement<String> {
    private String s;

    public JSONString(String s) {
        this.s = s;
    }

    @Override
    public String get() {
        return s;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.String;
    }

    @Override
    public void visit(final JSONVisitor visitor, final int depth) {
    }

    public String toString() {
        return s;
    }

    public int getAggregateSize() {
        return 10 + (s.length() * 2);
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeString(s);
    }
}