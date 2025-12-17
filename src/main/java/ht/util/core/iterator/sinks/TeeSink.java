package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class TeeSink<I> extends BaseSink<I> {
    protected Sink<I> sink1;
    protected Sink<I> sink2;

    public TeeSink(Sink<I> sink1, Sink<I> sink2) {
        this.sink1 = sink1;
        this.sink2 = sink2;
    }

    public static <I> Sink<I> get(Sink<I>... sinks) {
        Queue<Sink<I>> queue = new LinkedList<Sink<I>>();
        for (Sink<I> s : sinks) {
            ((LinkedList<Sink<I>>) queue).add(s);
        }
        while (queue.size() > 1) {
            queue.add(new TeeSink(queue.remove(), queue.remove()));
        }
        return ((LinkedList<Sink<I>>) queue).get(0);
    }

    @Override
    public boolean init(JsonNode node) {
        boolean success = sink1.init(node);
        success |= sink2.init(node);
        return success;
    }

    @Override
    public boolean start() throws IOException {
        boolean success = sink1.start();
        success |= sink2.start();
        return success;
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        sink1.add(o);
        sink2.add(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        boolean success = sink1.stop();
        success |= sink2.stop();
        return success;
    }
}
