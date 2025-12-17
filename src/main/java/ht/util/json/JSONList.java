package ht.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.json.visitors.JSONVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JSONList extends JSONElement<List<JSONElement>> {
    private List<JSONElement> list;

    public JSONList() {
        this.list = new ArrayList();
    }

    public JSONList(List<JSONElement> list) {
        this.list = list;
    }

    public CollectionIterator<JSONElement> iterate() {
        return new CollectionIterator(list);
    }

    public int size() {
        return list.size();
    }

    @Override
    public List<JSONElement> get() {
        return list;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.List;
    }

    @Override
    public int getAggregateSize() {
        int counter = 0;
        for (JSONElement elem : list) {
            counter += elem.getAggregateSize();
        }
        return counter;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (JSONElement elem : list) {
            if (elem.getJSONType() == JSONType.Map) {
                generator.writeStartObject();
                elem.write(generator);
                generator.writeEndObject();
            } else {
                elem.write(generator);
            }
        }
        generator.writeEndArray();
    }

    @Override
    public void visit(final JSONVisitor visitor, int depth) {
        int child = depth + 1;
        for (JSONElement elem : list) {
            elem.visit(visitor, child);
        }
    }

    public void add(JSONElement elem) {
        list.add(elem);
    }

    public void add(JSONElement... elems) {
        for (JSONElement elem : elems) {
            list.add(elem);
        }
    }
}
