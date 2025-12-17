package ht.util.core.iterator.queue;

import ht.util.core.queue.EmptyQueueCallback;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MappingDequeue<D, E> extends AbstractDequeue<E> {
    private AbstractDequeue<D> queue;
    private Function<D, E> mappingFunction;

    public MappingDequeue(AbstractDequeue<D> queue, Function<D, E> mappingFunction) {
        this.queue = queue;
        this.mappingFunction = mappingFunction;
    }

    public void setQueueEmptyCallback(EmptyQueueCallback q) {
        queue.setQueueEmptyCallback(q);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isCompleted() {
        return queue.isCompleted();
    }

    @Override
    public boolean getQueueCanceled() {
        return queue.getQueueCanceled();
    }

    @Override
    public E take() throws InterruptedException {
        return mappingFunction.apply(queue.take());
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        D d = queue.poll(timeout, unit);
        if (d == null) {
            return null;
        }
        return mappingFunction.apply(d);
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        int count = 0;
        while (queue.remainingCapacity() > 0) {
            try {
                c.add(mappingFunction.apply(queue.take()));
                count++;
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        int count = 0;
        while (queue.remainingCapacity() > 0 && count < maxElements) {
            try {
                c.add(mappingFunction.apply(queue.take()));
                count++;
            } catch (InterruptedException e) {

            }
        }
        return count;
    }
}
