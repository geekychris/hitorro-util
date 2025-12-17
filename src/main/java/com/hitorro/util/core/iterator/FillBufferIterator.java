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

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.Log;


public class FillBufferIterator<I, O> extends AbstractIterator<O> {
    private AbstractIterator<I> iter;
    private FillState state = FillState.Init;
    private GenericKeyValue<I, O> buffer[];
    private int batchSize;
    private int index = 0;
    private int currentSize = 0;
    private FillBufferHandler<I, O> transformer;

    public FillBufferIterator(AbstractIterator<I> inIter, int batchSize, FillBufferHandler<I, O> transformer) {
        this.iter = inIter;
        this.transformer = transformer;
        buffer = new GenericKeyValue[batchSize];
        for (int i = 0; i < batchSize; i++) {
            buffer[i] = new GenericKeyValue(null, null);
        }
        this.batchSize = batchSize;
    }

    public boolean hasNext() {
        if (state != FillState.Complete) {
            while (index < currentSize) {
                if (buffer[index].getValue() != null) {
                    return true;
                }
                index++;
            }
            try {
                return fillAndTransform();
            } catch (Exception e) {
                Log.util.error("Unable to fill buffer %s %e", e, e);
                return false;
            }
        }
        return false;
    }

    public O next() {
        return buffer[index++].getValue();
    }

    public void remove() {

    }

    private boolean fillAndTransform() throws Exception {
        if (grabMoreKeys()) {
            transformer.fill(buffer, currentSize);
            return true;
        }

        return false;
    }

    private boolean grabMoreKeys() throws Exception {
        if (state == FillState.Complete) {
            return false;
        }
        currentSize = 0;
        index = 0;
        for (int i = 0; i < batchSize; i++) {
            if (iter.hasNext()) {
                I curr = iter.next();
                if (curr != null) {
                    buffer[currentSize].setKey(curr);
                    currentSize++;
                }
            } else {
                state = FillState.LastRound;
                iter.close();
                break;
            }
        }
        if (currentSize == 0) {
            state = FillState.Complete;
            return false;
        }
        return true;
    }

    @Override
    public void close() throws Exception {
        iter.close();
    }


    enum FillState {
        Init, LastRound, Complete
    }
}
