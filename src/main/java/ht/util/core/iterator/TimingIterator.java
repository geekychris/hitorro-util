package ht.util.core.iterator;

import ht.util.servicecounters.CounterService;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.registers.DoubleDivideRegister;
import ht.util.servicecounters.registers.LongRegister;

import java.util.Iterator;

/**
 *
 */
public class TimingIterator<T> extends AbstractIterator<T> {
    private Iterator<T> iter;
    private CounterSet cs;
    private LongRegister callCount;
    private LongRegister accumulativeTime;
    private DoubleDivideRegister avgTime;
    private long time;

    public TimingIterator(CounterSet cs, Iterator<T> iterator) {
        this.iter = iterator;
        this.cs = cs;
        callCount = cs.getLongRegister("callcount", "Callcount");
        accumulativeTime = cs.getLongRegister("accumtime", "Accumulative time");
        avgTime = cs.getDoubleDivideRegister("avgTime", "Average time", accumulativeTime, callCount);
        cs.finishInit(CounterService.getService().getCounterContext());

    }

    @Override
    public void close() throws Exception {
        if (iter instanceof AutoCloseable) {
            ((AutoCloseable) iter).close();
        }
    }

    @Override
    public boolean hasNext() {
        time = System.currentTimeMillis();
        boolean ret = iter.hasNext();
        accumulativeTime.incrementBy(System.currentTimeMillis() - time);
        return ret;
    }

    @Override
    public T next() {
        time = System.currentTimeMillis();
        T ret = iter.next();
        accumulativeTime.incrementBy(System.currentTimeMillis() - time);
        callCount.incrementBy(1);
        return ret;
    }

    @Override
    public void remove() {
        iter.remove();
    }
}
