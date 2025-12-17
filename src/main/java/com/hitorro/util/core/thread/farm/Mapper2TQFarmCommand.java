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
package com.hitorro.util.core.thread.farm;

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 *
 */
public class Mapper2TQFarmCommand<InputType, IntermediateType, OutputType, ThreadDateType> extends FarmCommand<InputType, OutputType, ThreadDateType> {
    private Mapper<InputType, AbstractIterator<IntermediateType>> iterMapper;
    private Mapper<IntermediateType, OutputType> mapper;
    private AbstractEnqueue<OutputType> tq;
    private HTPredicate<OutputType> filter;

    public Mapper2TQFarmCommand(Mapper<InputType, AbstractIterator<IntermediateType>> iterMapper, Mapper<IntermediateType, OutputType> mapper,
                                HTPredicate<OutputType> filter, AbstractEnqueue<OutputType> tq) {
        this.iterMapper = iterMapper;
        this.mapper = mapper;
        this.tq = tq;
        this.filter = filter;
    }

    @Override
    public OutputType apply(final InputType inElement) {
        AbstractIterator<OutputType> iter = iterMapper.apply(inElement).map(mapper);
        if (filter != null) {
            iter = iter.filter(filter);
        }
        while (iter != null && iter.hasNext()) {
            try {
                tq.put(iter.next());
            } catch (InterruptedException e) {
            }
        }
        try {
            iter.close();
        } catch (IOException e) {

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}