package ht.util.core.iterator;

import ht.util.core.iterator.sinks.Sink;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class FilterToSinkIterator<T> extends AbstractIterator<T> {
    protected Sink<T> sink;
    private Predicate<T> filter;
    private Iterator<T> inIter;

    public FilterToSinkIterator(Iterator<T> inIter, Predicate<T> filter, Sink<T> sink) {
        this.filter = filter;
        this.sink = sink;
        this.inIter = inIter;
    }

    public boolean hasNext() {
        return inIter.hasNext();
    }

    public T next() {
        T tmp = inIter.next();
        if (filter.test(tmp)) {
            try {
                sink.add(tmp);
            } catch (IOException e) {
                //
            } catch (StoreException e) {
                //
            }
        }
        return tmp;
    }

    public void remove() {
        inIter.remove();
    }

    @Override
    public void close() throws Exception {
        if (inIter instanceof AutoCloseable) {
            ((AutoCloseable) inIter).close();
        }
    }
}
