package ht.util.core.iterator;

import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.core.thread.EnhancedThreadGroup;
import ht.util.core.thread.farm.Farm;
import ht.util.core.thread.farm.MapperFarmCommand;

import java.util.Iterator;
import java.util.function.Function;

/**
 *
 */
public class ParIterator<IN, OUT> extends AbstractIterator<OUT> implements Runnable {
    private Iterator<IN> iter;
    private Iterator<OUT> currIter;
    private NestingIteratorErrorHandler<IN, OUT> handler = new SurpressException();
    private AbstractIterator<OUT> outIter;

    private AbstractEnqueue<IN> inQ;
    private AbstractEnqueue<OUT> outQ;
    private MapperFarmCommand<IN, OUT> mfc;
    private EnhancedThreadGroup etg;
    private Farm<IN, OUT, Object> farm;
    private Thread thread;

    public ParIterator(Iterator<IN> initer,
                       Function<IN, OUT> mapper,
                       int threads, int queueSizeIn, int queueSizeOut, String name) {
        this.iter = initer;
        inQ = AbstractEnqueue.arrayBlocking(queueSizeIn);
        outQ = AbstractEnqueue.arrayBlocking(queueSizeOut);
        //mfc = new Mapper2TQFarmCommand(iterMapper, me, filter, outQ);
        mfc = new MapperFarmCommand(mapper);
        etg = new EnhancedThreadGroup(name);

        farm = new Farm(name, etg, inQ, outQ, mfc, threads);
        farm.start();
        thread = new Thread(etg, this);
        thread.start();
        outIter = outQ.dequeue().iterator();
    }

    @Override
    public void close() throws Exception {
        close(iter);
    }

    public void run() {
        while (getIterator()) {

        }
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
                    inQ.put(iter.next());
                    return true;
                }
                inQ.setQueueComplete();
                return false;
            } catch (Exception e) {
                if (!handler.continueExecution(iter, currIter, e)) {
                    return false;
                }
            }
        }
    }

    /**
     * Really we shouldn't be man handling the out iterator.  The above setup should be a factory or the
     * ThreadedQueueIterator should be part of this guy.
     *
     * @return
     */
    @Override
    public boolean hasNext() {
        return outIter.hasNext();
    }

    @Override
    public OUT next() {
        return outIter.next();
    }

    @Override
    public void remove() {
        if (currIter != null) {
            currIter.remove();
        }
    }
}





