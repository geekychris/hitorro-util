package ht.util.core.iterator;

import ht.util.core.Env;

import java.util.Iterator;

/**
 * Iterator responsible for retrying indefinitely the next method of an underlying iterator.  Used with iterators that
 * may temporarily start out of data, but are possibly recoverable.
 */
public class RetryingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> victim;
    private int sleep;
    private boolean running = true;

    public RetryingIterator(Iterator<E> victim, int sleep) {
        this.victim = victim;
        this.sleep = sleep;
    }

    public void close() throws Exception {
        if (victim instanceof AbstractIterator) {
            ((AbstractIterator) victim).close();
        }
    }

    public void stop() {
        running = false;
    }

    public boolean hasNext() {
        while (running && !victim.hasNext()) {
            Env.sleepNSeconds(sleep);
        }
        return running;
    }

    public E next() {
        return victim.next();
    }

    public void remove() {
        victim.remove();
    }
}

