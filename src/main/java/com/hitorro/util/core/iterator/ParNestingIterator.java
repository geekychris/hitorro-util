/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.iterator;

import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.core.thread.farm.Farm;
import com.hitorro.util.core.thread.farm.Mapper2TQFarmCommand;

import java.util.Iterator;

/**
 *
 */
public class ParNestingIterator<INITER, INTERMEDIATE, OUTITER> extends AbstractIterator<OUTITER> implements Runnable {
    private Iterator<INITER> iter;
    private Iterator<OUTITER> currIter;
    private NestingIteratorErrorHandler<INITER, OUTITER> handler = new SurpressException();
    private AbstractIterator<OUTITER> outIter;

    private AbstractEnqueue<INITER> inQ;
    private AbstractEnqueue<OUTITER> outQ;
    private Mapper2TQFarmCommand<INITER, INTERMEDIATE, OUTITER, Object> mfc;
    private EnhancedThreadGroup etg;
    private Farm<INITER, OUTITER, Object> farm;
    private Thread thread;
    private HTPredicate<OUTITER> filter;

    public ParNestingIterator(Iterator<INITER> iter, Mapper<INITER, Iterator<INTERMEDIATE>> iterMapper,
                              Mapper<INTERMEDIATE, OUTITER> mapper, HTPredicate<OUTITER> filter,
                              int threads, int queueSizeIn, int queueSizeOut, String name) {
        this.iter = iter;
        this.filter = filter;
        inQ = AbstractEnqueue.arrayBlocking(queueSizeIn);
        outQ = AbstractEnqueue.arrayBlocking(queueSizeOut);
        mfc = new Mapper2TQFarmCommand(iterMapper, mapper, filter, outQ);
        etg = new EnhancedThreadGroup(name);
        // provide out Q, but the farm doesnt write to it, the me does as there is a 1:n ratio o input to output.
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
    public OUTITER next() {
        return outIter.next();
    }

    @Override
    public void remove() {
        if (currIter != null) {
            currIter.remove();
        }
    }
}




