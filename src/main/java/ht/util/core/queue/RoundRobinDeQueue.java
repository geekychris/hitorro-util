package ht.util.core.queue;

import ht.util.core.BooleanUtil;
import ht.util.core.Log;
import ht.util.core.iterator.queue.AbstractDequeue;
import ht.util.core.iterator.queue.AbstractEnqueue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Uses a weighted round robin mechanism to allow multiple threaded queues to be read.  The idea is that you can specify
 * a number of slots to be taken from a particular queue.  When dequeueing, the max of n elements can be taken from a
 * particular channel before moving onto the next.  If that channel has less than n, the RRQ moves onto the next
 * earlier
 */
public class RoundRobinDeQueue<E> extends AbstractDequeue<E> {
    private AbstractDequeue<E> queues[];
    private int priorities[];
    private int currCounts[];
    private boolean completed[];
    private int index = 0;
    private boolean canceled = false;
    private boolean complete = false;
    private int count;
    private int channels;

    private String name;

    private Object notifier = new Object();
    private RoundRobinEnqueue<E> enqueue;

    public RoundRobinDeQueue(String name, RoundRobinEnqueue<E> enqueue, int priorities[]) {
        this.name = name;
        this.enqueue = enqueue;
        this.notifier = enqueue.getNotifier();
        AbstractEnqueue<E> arr[] = enqueue.getEnqueues();
        this.queues = new AbstractDequeue[arr.length];
        for (int i = 0; i < arr.length; i++) {
            queues[i] = arr[i].dequeue();
        }
        this.priorities = priorities;
        channels = priorities.length;
        this.currCounts = new int[channels];
        this.completed = BooleanUtil.getBooleanArray(channels, false);

        this.notifier = queues[0].getEnqueue().getNotifier();
        for (AbstractDequeue q : queues) {
            if (q.getEnqueue().getNotifier() != this.notifier) {
                Log.util.fatal("RoundRobinDeQueue requies all input queues to share their notifier object");
            }
        }
        setCount();
    }

    public Object getNotifier() {
        return notifier;
    }

    public void setNotifier(Object notifier) {
        this.notifier = notifier;
    }

    private boolean advance(boolean stopOnNotEmpty) {
        int counted = 0;
        while (counted < channels) {
            index = index + 1;

            if (index >= priorities.length) {
                index = 0;
            }
            if (completed[index] == false) {
                if (queues[index].getQueueCanceled()) {
                    // we are done, a single canceled channel is considered some kind of downstream
                    // rollback
                    canceled = true;
                    return false;
                }
                if (queues[index].getEnqueue().getQueueComplete()) {
                    completed[index] = true;
                    counted++;
                    continue;
                }
                if (stopOnNotEmpty && queues[index].size() == 0) {
                    counted++;
                    continue;
                }
                setCount();
                return true;
            } else {
                // skip this guy, keep looking
                counted++;
            }
        }
        /// more channels
        complete = true;
        return false;
    }

    private void setCount() {
        count = priorities[index];
    }

    synchronized public E take() throws InterruptedException {
        return poll(1000000000000000l, TimeUnit.MILLISECONDS);
    }

    synchronized public E peek() throws ThreadedQueueCanceledException, ThreadedQueueTimeoutException {
        return null;
    }

    public E poll(long timeout, final TimeUnit unit) throws InterruptedException {
        synchronized (notifier) {
            try {
                long entryTime = System.currentTimeMillis();

                while (waitingForValue()) {
                    notifier.wait(1000);
                    long elapsed = System.currentTimeMillis() - entryTime;
                    if (elapsed > timeout) {
                        // we timed out and we should return null;
                        throw new ThreadedQueueTimeoutException();
                    }
                    if (canceled) {
                        throw new ThreadedQueueCanceledException();
                    }
                }
            } catch (InterruptedException e) {
            }


            E e = queues[index].poll(timeout, unit);
            if (e != null) {
                count--;
                notifier.notify();
                return e;
            }
            return null;
        }
    }

    @Override
    public int remainingCapacity() {
        int count = 0;
        for (AbstractDequeue ad : queues) {
            count += ad.remainingCapacity();
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        int count = 0;
        while (this.remainingCapacity() > 0) {
            try {
                count++;
                c.add(take());
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        int count = 0;
        while (this.remainingCapacity() > 0 && count < maxElements) {
            try {
                count++;
                c.add(take());
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    private boolean waitingForValue() {
        if (count > 0) {
            // will take from current queue if it has something
            if (queues[index].size() > 0) {
                return false;
            }
        }
        return !advance(true);
    }

    public void put(E obj) throws ThreadedQueueTimeoutException, ThreadedQueueCanceledException {
        //not implemented
    }

    public int size() {
        synchronized (notifier) {
            int count = 0;
            for (int i = 0; i < queues.length; i++) {
                if (!completed[i]) {
                    count += queues[i].size();
                }
            }
            return count;
        }
    }

    @Override
    public boolean isCompleted() {
        for (boolean b : completed) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean getQueueCanceled() {
        return canceled;
    }

    public void setQueueCanceled(boolean flag) {

    }

    public void setQueueComplete() {
        complete = true;
    }

    public boolean getQueueComplete() {
        return complete;
    }

    public String getQueueName() {
        return name;
    }

    public boolean isEmpty() {
        for (int i = index; i < queues.length + index; i++) {
            int index = i % queues.length;
            if (!completed[index]) {
                if (queues[index].size() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isFull() {
        for (int i = index; i < queues.length + index; i++) {
            int index = i % queues.length;
            if (!completed[index]) {
                if (queues[index].remainingCapacity() > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}

