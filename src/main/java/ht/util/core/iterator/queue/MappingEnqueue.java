package ht.util.core.iterator.queue;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MappingEnqueue<D, E> extends AbstractEnqueue<D> {
    protected AbstractEnqueue<E> queue;
    protected Function<D, E> mappingFunction;

    public MappingEnqueue(AbstractEnqueue<E> queue, Function<D, E> mappingFunction) {
        this.queue = queue;
        this.mappingFunction = mappingFunction;
    }


    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean add(final D d) {
        return queue.add(mappingFunction.apply(d));
    }

    @Override
    public void close() throws IOException {
        queue.close();
    }

    @Override
    public boolean offer(final D d) {
        return queue.offer(mappingFunction.apply(d));
    }

    @Override
    public void put(final D d) throws InterruptedException {
        queue.put(mappingFunction.apply(d));
    }

    @Override
    public boolean offer(final D d, final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.offer(mappingFunction.apply(d), timeout, unit);
    }

    public int size() {
        return queue.size();
    }

    @Override
    public AbstractDequeue<D> dequeue() {
        //XXX not sure what to
        return null;
    }

    @Override
    public void clear() {
        queue.clear();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public Object getNotifier() {
        return queue.getNotifier();
    }

    @Override
    public void setNotifier(final Object notifier) {
        queue.setNotifier(notifier);
    }

    @Override
    public boolean getQueueCanceled() {
        return queue.getQueueCanceled();
    }

    @Override
    public void setQueueCanceled(final boolean flag) {
        queue.setQueueCanceled(flag);
    }

    @Override
    public void setQueueComplete() {
        queue.setQueueComplete();
    }

    @Override
    public boolean getQueueComplete() {
        return queue.getQueueComplete();
    }

    @Override
    public D remove(final Object o) {
        E e = queue.remove(o);
        // dont have the reverse function!!!!
        return null;
    }

}
