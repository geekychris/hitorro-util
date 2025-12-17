package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dump elements to a listFiles for recovery later.
 */
public class SinkList<E> implements Sink<E> {
    private List<E> list = new ArrayList();

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        list.clear();
        return true;
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        list.add(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        return true;
    }

    @Override
    public void close() throws IOException {
        //
    }

    public List<E> getList() {
        return list;
    }
}
