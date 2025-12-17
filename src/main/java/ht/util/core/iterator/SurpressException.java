package ht.util.core.iterator;

import ht.util.core.Log;

import java.util.Iterator;

/**
 *
 */
public class SurpressException<INITER, OUTITER> implements NestingIteratorErrorHandler<INITER, OUTITER> {

    @Override
    public boolean continueExecution(final Iterator<INITER> iter, final Iterator<OUTITER> out, final Exception e) {
        Log.iterator.error("Received but continuing exception processing %s %s, error %s %e",
                iter.getClass().getCanonicalName(),
                iter, e, e);
        return true;
    }
}
