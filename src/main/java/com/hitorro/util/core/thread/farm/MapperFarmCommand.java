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

import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.util.function.Function;

/**
 * Brain dead farm command that takes a Mapper to do its mapping.  This is a limiting command as it does not give access
 * to thread local information that can be leveraged in a multi threaded type situation.
 */
public class MapperFarmCommand<InputType, OutputType> extends FarmCommand<InputType, OutputType, BaseMapper<InputType, OutputType>> {
    private Function<InputType, OutputType> mapper;

    private BaseMapper<InputType, OutputType> baseMapper;
    private boolean isBaseMapper = false;

    public MapperFarmCommand(Function<InputType, OutputType> mapper) {
        this.mapper = mapper;
        if (mapper instanceof BaseMapper) {
            isBaseMapper = true;
            baseMapper = (BaseMapper<InputType, OutputType>) mapper;
        }
    }

    @Override
    public OutputType apply(final InputType inElement) {
        if (isBaseMapper) {
            if (baseMapper.isThreadSafe()) {
                return mapper.apply(inElement);
            } else {
                BaseMapper<InputType, OutputType> m = this.getThreadData();
                if (m == null) {
                    m = baseMapper.getCopy();
                    this.setThreadData(m);
                }
                return m.apply(inElement);
            }
        } else {
            return mapper.apply(inElement);
        }
    }

    public void deinit(FarmThread ft) {
        super.deinit(ft);
        if (isBaseMapper) {
            baseMapper.close();
        }
    }
}
