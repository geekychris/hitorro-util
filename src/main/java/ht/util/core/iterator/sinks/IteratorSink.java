package ht.util.core.iterator.sinks;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 */
public interface IteratorSink<T> {
    /**
     * Set the iterator to pull from.
     *
     * @param iter
     */
    void setIterator(Iterator<T> iter);

    /**
     * Suck all the contents out of the iterator until there is no more, once complete, you should honor the
     * shouldCloseOnCompletion
     *
     * @return number of objects output.
     */
    int sink() throws IOException;
}
