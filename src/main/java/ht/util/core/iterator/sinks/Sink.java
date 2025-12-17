package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.StoreException;
import ht.util.json.JsonInitable;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Place where a sync of some kind of processing queue can send its data to (if we are in some kind of vectored queue
 * mode.
 */
public interface Sink<T> extends AutoCloseable, JsonInitable, Consumer<T> {
    boolean init(JsonNode node);

    boolean start() throws IOException;

    default Sink<T> maxPerTransaction(long max) {
        return new MaxItemsPerTransactionSink<>(this, max);
    }

    default Sink<T> filter(Predicate<T> predicate) {
        return new PredicatedSink(this, predicate);
    }

    default <I> Sink<I> map(Function<I, T> function) {
        return new MappingSink(this, function);
    }

    default Sink<T> tee(Sink<T> other) {
        return new TeeSink<>(this, other);
    }

    /**
     * Make consumer friendly
     *
     * @param t
     */
    default void accept(T t) {
        try {
            add(t);
        } catch (IOException | StoreException e) {
            //
        }

    }

    boolean add(T o) throws IOException, StoreException;

    default boolean addAll(final Collection<T> oList) throws IOException, StoreException {
        boolean success = true;
        for (T o : oList) {
            if (!add(o)) {
                success = false;
            }
        }
        return success;
    }

    boolean stop() throws IOException;

    void close() throws IOException;

    default Sink<T> merge(Sink<T> in) {
        return in;
    }
}
