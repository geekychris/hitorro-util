package ht.util.core.iterator;

import ht.util.core.Constants;
import ht.util.core.Env;

import java.io.IOException;
import java.util.Iterator;

/**
 * Prevents a consumer from consuming content beyond a certain rpm.
 */
public class RateLimitingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> iter;
    private int rpm;
    private long offset;
    private long start;

    public RateLimitingIterator(int rpm, Iterator<E> iterator) {
        this.iter = iterator;
        this.rpm = rpm;
        this.offset = Constants.MillisInMinute / rpm;
        this.start = System.currentTimeMillis() - offset;
    }

    @Override
    public void close() throws IOException {
        if (iter instanceof CloseableIterator) {
            try {
                AbstractIterator.attemptClose(iter);
            } catch (Exception e) {
                return;
            }
        }
    }

    public boolean hasNext() {
        long delta = offset - (System.currentTimeMillis() - start);

        if (delta > 0) {
            Env.sleepMillis(delta);
        }
        start = System.currentTimeMillis();
        return iter.hasNext();
    }

    public E next() {
        E ret = iter.next();
        return ret;
    }

    public void remove() {
        iter.remove();
    }
}
