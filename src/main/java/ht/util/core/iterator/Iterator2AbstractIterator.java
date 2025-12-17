package ht.util.core.iterator;

import java.util.Iterator;

public class Iterator2AbstractIterator<E> extends AbstractIterator<E> {
    private Iterator<E> iter;

    public Iterator2AbstractIterator(Iterator<E> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public E next() {
        return iter.next();
    }
}
