package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.function.Predicate;

public class PredicatedSink<T> implements Sink<T> {
    private Sink<T> sink;
    private Predicate<T> predicate;

    public PredicatedSink(Sink<T> sink, Predicate<T> predicate) {
        this.sink = sink;
        this.predicate = predicate;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        return sink.start();
    }

    @Override
    public boolean add(final T o) throws IOException, StoreException {
        if (predicate.test(o)) {
            this.sink.add(o);
        }
        return false;
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }

    @Override
    public void close() throws IOException {
        this.sink.close();
    }
}
