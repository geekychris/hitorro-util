package ht.util.core.iterator;

import java.io.IOException;

/**
 *
 */
public class NullIterator<T> extends AbstractIterator<T> {
    public static final NullIterator me = new NullIterator();


    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public void remove() {
    }

    @Override
    public void close() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
