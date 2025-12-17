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
package com.hitorro.util.core.iterator.reducers;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.BaseReducer;

import java.util.Comparator;

public class TupleComparatorReducer<E> implements BaseReducer<GenericKeyValue<E, E>, GenericKeyValue<Integer, Integer>> {

    private Comparator<E> comp;

    public TupleComparatorReducer(Comparator<E> comp) {
        this.comp = comp;
    }

    @Override
    public GenericKeyValue<Integer, Integer> reduce(AbstractIterator<GenericKeyValue<E, E>> iter) {
        int eq = 0;
        int neq = 0;
        while (iter.hasNext()) {
            GenericKeyValue<E, E> elem = iter.next();
            if (comp.compare(elem.getKey(), elem.getValue()) == 0) {
                eq++;
            } else {
                neq++;
            }
        }
        return new GenericKeyValue(eq, neq);
    }
}
