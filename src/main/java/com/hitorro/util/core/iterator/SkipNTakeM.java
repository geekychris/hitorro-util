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

import com.hitorro.util.core.opers.AlwaysTrueOperator;

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
