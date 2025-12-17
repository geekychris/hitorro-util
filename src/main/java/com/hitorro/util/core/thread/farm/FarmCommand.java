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

import com.hitorro.util.core.DeInitIntf;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

/**
 * Command that performs the work on a queue element. Given Type I and returns O I in most cases be the same type as O
 */
public abstract class FarmCommand<InputType, OutputType, ThreadDateType> extends BaseMapper<InputType, OutputType> {
    /**
     * Unit of work to be carried out. This command must allign whith the input queue and output queue.
     *
     * @param inElement
     * @return
     */
    public abstract OutputType apply(InputType inElement);

    public ThreadDateType getThreadData() {
        return (ThreadDateType) ((FarmThread) Thread.currentThread()).getThreadData();
    }

    public void setThreadData(ThreadDateType data) {
        ((FarmThread) Thread.currentThread()).setThreadData(data);
    }

    public ThreadDateType getThreadData(FarmThread ft) {
        return (ThreadDateType) ft.getThreadData();
    }

    public void commit() {
        // subclass expected to make sense of a commit if it assumes that the command was used in some
        // kind of transactional process.
    }

    public void rollback() {
        // subclass expected to make sense of a rollback if it assumes that the command was used in some
        // kind of transactional process.
    }

    /**
     * If the payload implements the DeInit interface then we call it
     *
     * @param ft
     */
    public void deinit(FarmThread ft) {
        ThreadDateType fdt = getThreadData(ft);
        if (fdt != null && fdt instanceof DeInitIntf) {
            ((DeInitIntf) fdt).deinit();
        }
    }

}
