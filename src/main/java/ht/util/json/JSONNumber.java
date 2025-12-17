package ht.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.core.NumberClassEnum;
import ht.util.json.visitors.JSONVisitor;

import java.io.IOException;

/**
 *
 */
public class JSONNumber extends JSONElement<Number> {
    private Number s;

    public JSONNumber(Number s) {
        this.s = s;
    }

    @Override
    public Number get() {
        return s;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.Number;
    }

    @Override
    public void visit(final JSONVisitor visitor, final int depth) {
    }

    public String toString() {
        return s.toString();
    }

    public int getAggregateSize() {
        return 10;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        NumberClassEnum e = NumberClassEnum.get(s);
        switch (e) {
            case FloatE:
                generator.writeNumber(s.floatValue());
                return;
            case DoubleE:
                generator.writeNumber(s.doubleValue());
                return;
            case ShortE:
                generator.writeNumber(s.shortValue());
                return;
            case IntegerE:
                generator.writeNumber(s.intValue());
                return;
            case LongE:
                generator.writeNumber(s.longValue());
                return;
        }
    }
}