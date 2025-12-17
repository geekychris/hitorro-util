package ht.util.core.iterator;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 */
public class NullRemovingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> t;
    private boolean first = true;

    private E e = null;

    public NullRemovingIterator(Iterator<E> t) {
        this.t = t;
    }

    public boolean hasNext() {
        if (first) {
            first = false;
            advanceTillNotNull();
        }
        return e != null;
    }

    public E next() {
        first = true;
        return e;
    }

    public void remove() {
        t.remove();
    }

    @Override
    public void close() throws IOException {
        try {
            if (t instanceof AutoCloseable) {
                ((AutoCloseable) t).close();
            }
        } catch (Exception e1) {
            return;
        }
    }

    private void advanceTillNotNull() {
        e = null;
        while (t.hasNext()) {
            e = t.next();
            if (e != null) {
                return;
            }
        }
    }
}
