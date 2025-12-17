package ht.util.core.iterator.queue;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class FilteringEnqueue<E> extends BaseEnqueue<E> {

    protected Predicate<E> predicate;

    public FilteringEnqueue(AbstractEnqueue<E> queue, Predicate<E> predicate) {
        this.queue = queue;
        this.predicate = predicate;
    }

    @Override
    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }

    @Override
    public AbstractDequeue<E> dequeue() {
        return queue.dequeue();
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
    public E remove(final Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean add(final E e) {
        if (!predicate.test(e)) {
            return false;
        }
        return queue.add(e);
    }

    @Override
    public void close() throws IOException {
        queue.close();
    }

    @Override
    public boolean offer(final E e) {
        if (!predicate.test(e)) {
            return false;
        }

        return queue.offer(e);
    }

    @Override
    public void put(final E e) throws InterruptedException {
        if (!predicate.test(e)) {
            return;
        }
        queue.put(e);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        if (!predicate.test(e)) {
            return false;
        }

        return queue.offer(e, timeout, unit);
    }
}
