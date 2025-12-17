package ht.util.json.iterators;

import ht.util.core.iterator.AbstractIterator;
import ht.util.json.JSONElement;
import ht.util.json.JSONList;

import java.util.List;

/**
 *
 */
public class JSONIterFromJSONList extends AbstractIterator<JSONElement> {
    private List<JSONElement> list;
    private int pos = 0;

    public JSONIterFromJSONList(JSONElement list) {
        this.list = ((JSONList) list).get();

    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        return list.size() > pos;
    }

    @Override
    public JSONElement next() {
        return list.get(pos++);
    }

    @Override
    public void remove() {
    }
}
