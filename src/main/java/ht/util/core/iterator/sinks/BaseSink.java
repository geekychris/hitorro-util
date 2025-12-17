package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.Mapper;
import ht.util.core.queue.ThreadedQueue;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 *
 */
public abstract class BaseSink<E> implements Sink<E> {
    /**
     * Create an asynchronous put.
     *
     * @param sink
     * @param queueLength
     * @param sendCompleteOnStop
     * @param name
     * @return
     */
    public static <E> ThreadedQueueSink<E> async(Sink<E> sink, int queueLength, boolean sendCompleteOnStop,
                                                 String name, boolean startThread) {
        ThreadedQueue tq = new ThreadedQueue(queueLength);
        ThreadedQueueSink tqs = new ThreadedQueueSink(tq, sendCompleteOnStop);
        SinkFromThreadedQueue dest = new SinkFromThreadedQueue(tq, sink, name, startThread);
        tqs.setPeer(dest);
        return tqs;
    }

    public abstract boolean init(JsonNode map);

    @Override
    public abstract boolean start() throws IOException;

    @Override
    public abstract boolean add(final E o) throws IOException, StoreException;

    @Override
    public abstract boolean stop() throws IOException;

    /**
     * We wrapper this Sink with a target of E with a mapper that is given (I) This works backwards from iterators, from
     * target item encapsulating to whatever ultimately calls the put
     *
     * @param mapper
     * @param <O>
     * @return
     */
    public <O> MappingSink<O, E> map(Mapper<O, E> mapper) {
        return new MappingSink<O, E>(this, mapper);
    }

    /**
     * Create an asynchronous put.  If you dont want to use a simple thread, then you can pass false for the threadstart
     * and then grab the peer from the returned object so you can pass the runnable to whatever mechanism
     *
     * @param queueLength
     * @param sendCompleteOnStop
     * @param name
     * @return
     */
    public ThreadedQueueSink<E> async(int queueLength, boolean sendCompleteOnStop, String name, boolean startThread) {
        return BaseSink.async(this, queueLength, sendCompleteOnStop, name, startThread);
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

}
