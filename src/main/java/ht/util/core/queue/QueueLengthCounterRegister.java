package ht.util.core.queue;

import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.registers.LongImplementableRegister;

/**
 * Register for the counter system that provides the length of a queue.
 */
public class QueueLengthCounterRegister extends LongImplementableRegister {
    private AbstractEnqueue q;

    public QueueLengthCounterRegister(final CounterSet cs, final String name, final String description, AbstractEnqueue q) {
        super(cs, name, description);
        this.q = q;
    }

    public long getAsLong() {
        return q.size();
    }
}
