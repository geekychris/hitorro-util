package ht.util.core.iterator;

import java.util.Iterator;

/**
 * Imagine you have an iterator of files and need to iterate through all the lines of each file.  You would want to have
 * an iterator that gave you the lines of the first file, the lines of the second file and so on.  This iterator nests
 * two iterators
 */
public class NestingIterator<INITER, OUTITER> extends AbstractIterator<OUTITER> {
    private Iterator<INITER> iter;
    private Mapper<INITER, Iterator<OUTITER>> mapper;
    private Iterator<OUTITER> currIter;
    private NestingIteratorErrorHandler<INITER, OUTITER> handler = new SurpressException();


    public NestingIterator(Iterator<INITER> iter, Mapper<INITER, Iterator<OUTITER>> mapper, NestingIteratorErrorHandler<INITER, OUTITER> handler) {
        if (handler == null) {
            handler = new SurpressException();
        }
        this.iter = iter;
        this.mapper = mapper;
        getIterator();
    }

    @Override
    public void close() throws Exception {
        close(iter);
    }

    /**
     * Take whatever is coming from the input iterator and apply it to iterator we are deligating to
     *
     * @return
     */
    private boolean getIterator() {
        while (true) {
            try {
                if (currIter != null) {
                    if (currIter instanceof CloseableIterator) {
                        ((CloseableIterator) currIter).close();
                    }
                }
                currIter = null;
                if (iter.hasNext()) {
                    INITER i = iter.next();
                    currIter = mapper.apply(i);
                }
                return currIter != null;
            } catch (Exception e) {
                if (!handler.continueExecution(iter, currIter, e)) {
                    return false;
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        return currIter != null && currIter.hasNext();
    }

    @Override
    public OUTITER next() {
        while (true) {
            try {
                OUTITER o = currIter.next();
                if (!currIter.hasNext()) {
                    // we dont have anything on this iterator, advance to the next one.
                    getIterator();
                }
                return o;
            } catch (Exception e) {
                if (!handler.continueExecution(iter, currIter, e)) {
                    return null;
                }
            }
        }
    }

    @Override
    public void remove() {
        if (currIter != null) {
            currIter.remove();
        }
    }
}




