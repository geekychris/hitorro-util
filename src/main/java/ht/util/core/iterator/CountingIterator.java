package ht.util.core.iterator;

import ht.util.core.Log;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class CountingIterator<E> extends Iterator2AbstractIterator<E> {

    private AtomicLong aLong;
    private long reportPeriodically = 0;
    private long prevTime = System.currentTimeMillis();

    public CountingIterator(Iterator<E> iter, AtomicLong aLong, long reportPeriodically) {
        super(iter);
        this.aLong = aLong;
        this.reportPeriodically = reportPeriodically;

    }

    @Override
    public E next() {
        aLong.incrementAndGet();
        if (reportPeriodically != 0 && aLong.get() % reportPeriodically == 0) {
            long n = System.currentTimeMillis();
            long d = n - prevTime;
            if (d > 1000) {
                d = d / 1000;
                long itemsPerSec = reportPeriodically / d;
                Log.util.info("Count %s, items %s per second", aLong.get(), itemsPerSec);
            } else {
                long itemsPerMilli = reportPeriodically / d;
                Log.util.info("Count %s, items %s per milli", aLong.get(), itemsPerMilli);
            }
            prevTime = n;

        }
        return super.next();
    }
}