package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.function.Consumer;

public class MaxItemsPerTransactionSink<E> implements Sink<E> {
    private long counter;
    private long maxPerTransaction;
    private Sink<E> in;
    private boolean started = false;
    private Consumer<Sink<E>> postCloseConsumer = null;

    public MaxItemsPerTransactionSink(Sink<E> in, long maxItems) {
        this(in, maxItems, null);
    }

    public MaxItemsPerTransactionSink(Sink<E> in, long maxItems, Consumer<Sink<E>> postCloseConsumer) {
        this.in = in;
        this.maxPerTransaction = maxItems;
        this.postCloseConsumer = postCloseConsumer;
        resetCounter();

    }

    private void resetCounter() {
        counter = maxPerTransaction;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        resetCounter();
        started = true;
        return in.start();
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        if (!started) {
            start();
        }
        if (counter == 0) {
            in.stop();
            postStopProcessor();
            in.start();
            resetCounter();
        }
        counter--;
        return in.add(o);
    }

    @Override
    public boolean stop() throws IOException {
        started = false;
        return in.stop();
    }

    @Override
    public void close() throws IOException {
        in.close();
        postStopProcessor();
    }

    private void postStopProcessor() {
        if (postCloseConsumer != null) {
            postCloseConsumer.accept(in);
        }
    }
}
