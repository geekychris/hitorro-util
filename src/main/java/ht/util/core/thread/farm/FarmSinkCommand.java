package ht.util.core.thread.farm;

import ht.util.core.iterator.sinks.Sink;

/**
 * Sink command (used by the farm put)
 *
 * @param <I>
 * @author chris
 */
public abstract class FarmSinkCommand<I> implements Sink<I> {
    public abstract boolean consume(I object);

    public abstract void close();
}
