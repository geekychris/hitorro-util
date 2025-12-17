package ht.util.core.iterator;

import java.util.Collection;

/**
 *
 */
public class CollectionIterator<E> extends Iterator2AbstractIterator<E> {
    public CollectionIterator(Collection<E> list) {
        super(list.iterator());
    }

    @Override
    public void close() throws Exception {
    }
}
