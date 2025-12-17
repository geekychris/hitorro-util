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
package com.hitorro.util.core.iterator.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;

public class FilterBlockingQueue<E> extends AbstractBlockingQueue<E> {
    private Predicate<E> predicate;

    FilterBlockingQueue(BlockingQueue<E> queue, Predicate<E> predicate) {
        super(queue);
        this.predicate = predicate;
    }

    @Override
    public boolean add(final E e) {
        if (predicate.test(e)) {
            return queue.add(e);
        }
        return false;
    }


    @Override
    public boolean addAll(final Collection<? extends E> c) {
        List<E> filtered = new ArrayList();

        for (E e : c) {
            if (predicate.test(e)) {
                filtered.add(e);
            }
            if (!filtered.isEmpty()) {
                return queue.addAll(filtered);
            }
        }
        return false;
    }

}
