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
package com.hitorro.util.core.iterator.mappers;

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;

/**
 *
 */
public abstract class BaseMapper<I, O> implements Mapper<I, O> {
    public boolean isThreadSafe() {
        return true;
    }

    /**
     * overide this function if you require cleanup
     */
    public void close() {

    }

    public BaseMapper getCopy() {
        return this;
    }

    public Class inputType() {
        return Object.class;
    }

    public Class outputType() {
        return Object.class;
    }

    public String getDescription() {
        return "description not defined";
    }

    public <E> BaseMapper<I, E> combine(BaseMapper<O, E> mapper) {
        if (mapper instanceof DummyBaseMapper) {
            //XXX Hack to see if I can work around a generics flaw I dont understand where HTSerializable cant seem to be use as a ? extends HTSerializable
            return (BaseMapper<I, E>) this;
        }
        return new MapperCollection(this, mapper);
    }


    public <E> BaseMapper<I, O> combineIterMapping(BaseMapper<AbstractIterator<O>, AbstractIterator<E>> iteratorMapper, BaseMapper<E, O> mapper) {
        return new IterMapMapper(mapper);
    }
}

class IterMapMapper<I, O> extends BaseMapper<AbstractIterator<I>, AbstractIterator<O>> {
    private BaseMapper<I, O> mapper;

    public IterMapMapper(BaseMapper<I, O> mapper) {
        this.mapper = mapper;
    }

    @Override
    public AbstractIterator<O> apply(final AbstractIterator<I> e) {
        return e.map(mapper);
    }
}
