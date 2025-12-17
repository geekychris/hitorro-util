package ht.util.core.iterator.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

public class FilterBlockingQueue<E> extends AbstractBlockingQueue<E> {
    private Predicate<E> predicate;

    FilterBlockingQueue(BlockingQueue<E> queue, Predicate<E> predicate) {
        super(queue);
        this.predicate = predicate;
    }

    @Override
    public boolean add(final E e) {
        if (predicate.test(e)) {
            return queue.add(e);
        }
        return false;
    }


    @Override
    public boolean addAll(final Collection<? extends E> c) {
        List<E> filtered = new ArrayList();

        for (E e : c) {
            if (predicate.test(e)) {
                filtered.add(e);
            }
            if (!filtered.isEmpty()) {
                return queue.addAll(filtered);
            }
        }
        return false;
    }

}
