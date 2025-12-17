package ht.util.core.iterator;

import java.util.List;

/**
 *
 */
public class BaseArrayListIterator<E> extends AbstractIterator<E> {
    protected List<E> e;
    protected int pos = 0;

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        return e.size() > pos;
    }

    @Override
    public E next() {
        return e.get(pos++);
    }

    @Override
    public void remove() {
    }
}
