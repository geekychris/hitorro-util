package ht.util.core.iterator;

import java.util.Iterator;

/**
 * provide a means for the nesting iterator to skip over bad "stuff"
 */
public interface NestingIteratorErrorHandler<INITER, OUTITER> {
    boolean continueExecution(Iterator<INITER> iter, Iterator<OUTITER> out, Exception e);
}
