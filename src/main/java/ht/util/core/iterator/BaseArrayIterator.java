package ht.util.core.iterator;

/**
 *
 */
public abstract class BaseArrayIterator<E> extends AbstractIterator<E> {
    protected E e[];
    protected int pos = 0;
    protected int maxPos;

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        return e.length > pos && maxPos >= pos;
    }

    @Override
    public E next() {
        return e[pos++];
    }

    @Override
    public void remove() {
    }
}

