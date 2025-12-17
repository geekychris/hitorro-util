package ht.util.core.iterator;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by chris on 5/16/16.
 */
public class TimeLimitingIterator<T> extends AbstractIterator<T> {
    private Iterator<T> iter;
    private long timeLimit;

    public TimeLimitingIterator(long timeLimit, Iterator<T> iterator) {
        this.iter = iterator;
        this.timeLimit = System.currentTimeMillis() + timeLimit;
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
        if (timeLimit < System.currentTimeMillis()) {
            return false;
        }
        return iter.hasNext();
    }

    public T next() {
        T ret = iter.next();
        return ret;
    }

    public void remove() {
        iter.remove();
    }
}
