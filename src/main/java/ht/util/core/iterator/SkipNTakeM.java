package ht.util.core.iterator;

import ht.util.core.opers.AlwaysTrueOperator;

import java.util.Iterator;
import java.util.function.Predicate;

public class SkipNTakeM<T> extends AbstractIterator<T> {
    private Iterator<T> iterIn;
    private long afterIncluding;
    private long take;
    private Predicate<T> excludePredicate;
    private T nextVal = null;
    private boolean expungeRemainder;

    public SkipNTakeM(Iterator<T> iterIn, long after, long take, Predicate<T> excludeFromCounting, boolean expungeRemainder) {
        this.iterIn = iterIn;
        this.take = take;
        this.afterIncluding = after + 1;
        this.expungeRemainder = expungeRemainder;
        if (excludeFromCounting != null) {
            this.excludePredicate = excludeFromCounting;
        } else {
            this.excludePredicate = AlwaysTrueOperator.oper;
        }
    }

    @Override
    public void close() throws Exception {
        AbstractIterator.attemptClose(iterIn);
    }

    public T next() {
        return nextVal;
    }

    public boolean hasNext() {
        if (afterIncluding > 0) {
            while (afterIncluding > 0 && iterIn.hasNext()) {
                nextVal = iterIn.next();
                if (!excludePredicate.test(nextVal)) {
                    continue;
                }
                afterIncluding--;
            }
            if (afterIncluding > 0) {
                nextVal = null;
                return false;
            }
        }
        if (take > 0) {
            while (iterIn.hasNext()) {
                nextVal = iterIn.next();
                if (!excludePredicate.test(nextVal)) {
                    continue;
                }
                take--;
                return true;
            }
            return false;
        }
        if (expungeRemainder) {
            return expungeTail();
        }
        return false;
    }

    protected boolean expungeTail() {
        while (iterIn.hasNext()) {
            nextVal = iterIn.next();
            if (!excludePredicate.test(nextVal)) {
                return true;
            }
        }
        nextVal = null;
        return false;
    }

    public void remove() {
    }
}
