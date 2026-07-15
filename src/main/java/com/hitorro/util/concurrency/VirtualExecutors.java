/*
 * Copyright (c) 2006-2026 Chris Collins
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
package com.hitorro.util.concurrency;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory methods for virtual-thread-based executors (Java 21+).
 *
 * <p>Prefer {@link #virtualPerTask()} for I/O-bound work. Names carry over as thread names
 * for debugging and thread dumps.
 */
public final class VirtualExecutors {

    private VirtualExecutors() {}

    /** New unbounded virtual-thread-per-task executor with default naming. */
    public static ExecutorService virtualPerTask() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /** New unbounded virtual-thread-per-task executor whose threads are named {@code namePrefix-N}. */
    public static ExecutorService virtualPerTask(String namePrefix) {
        return Executors.newThreadPerTaskExecutor(virtualFactory(namePrefix));
    }

    /** Thread factory producing virtual threads named {@code namePrefix-N}. */
    public static ThreadFactory virtualFactory(String namePrefix) {
        AtomicLong counter = new AtomicLong();
        return r -> Thread.ofVirtual()
                .name(namePrefix + "-" + counter.incrementAndGet())
                .unstarted(r);
    }
}
