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
package com.hitorro.util.statemachine.scoreboard;

import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.core.thread.farm.Farm;
import com.hitorro.util.core.thread.farm.FarmCommand;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:13:15 PM
 * <p/>
 * Container of state rows.  These rows hold a state and an object (payload) that the state belongs to.
 */
public class ScoreBoard<P, R extends StateRow<P>> {
    private List<R> m_rows = new LinkedList<R>();

    private int m_numberWorkerThreads = 4;


    private AbstractEnqueue<WorkerElement<P>> m_queueIn = null;

    private AbstractEnqueue<WorkerElement<P>> m_queueOut = null;
    private EnhancedThreadGroup m_threadGroup = new EnhancedThreadGroup("ScoreBoard");

    private Farm m_farm;

    public ScoreBoard(int threads) {
        m_numberWorkerThreads = threads;
    }

    public int size() {
        return m_rows.size();
    }

    public void add(R row) {
        m_rows.add(row);
    }

    /**
     * @param constraint
     * @param action
     */
    public void visit(HTPredicate<StateRow<P>> constraint, StateRowAction<P> action) {
        visit(constraint, action, false);
    }

    private void setupWorker(int numberThreads) {
        if (m_queueIn == null) {
            synchronized (this) {
                m_queueIn = AbstractEnqueue.arrayBlocking(numberThreads * 2);
                // should not really need this as the farm element command will return null;
                m_queueOut = AbstractEnqueue.arrayBlocking(numberThreads * 2);
                m_farm = new Farm("ScoreBoard", m_threadGroup, m_queueIn, m_queueOut, new ScoreBoardCommand<P>(), 40);
            }
        }
    }

    public void visit(HTPredicate<StateRow<P>> constraint, StateRowAction<P> action, boolean useQueue) {
        Iterator<R> iter = m_rows.iterator();

        while (iter.hasNext()) {
            R row = iter.next();
            if (row.isDeleted()) {
                iter.remove();
            } else {
                if (constraint.test(row)) {
                    if (useQueue) {
                        try {
                            setupWorker(m_numberWorkerThreads);
                            m_queueIn.put(new WorkerElement<P>(action, row));
                        } catch (InterruptedException e) {
                        }
                    } else {
                        // meets criteria now perform action.
                        action.execute(row);
                    }
                }
            }
        }
    }
}


class ScoreBoardCommand<P> extends FarmCommand<WorkerElement<P>, WorkerElement<P>, Object> {
    public WorkerElement<P> apply(WorkerElement<P> inElement) {
        inElement.execute();
        return null;
    }
}