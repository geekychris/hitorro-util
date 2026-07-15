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
package com.hitorro.util.tracing;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Snapshot of the current thread's SLF4J MDC that can be applied on another thread.
 *
 * <p>Because virtual threads and executor-submitted tasks run on a different carrier than the
 * caller, MDC state — including {@link CorrelationId} — does <b>not</b> propagate automatically
 * across the submit boundary. Wrap the task body with {@link #wrap(Runnable)} or
 * {@link #wrap(Callable)} to carry the caller's MDC over:
 *
 * <pre>{@code
 * MdcContext ctx = MdcContext.capture();
 * exec.submit(ctx.wrap(() -> doWork()));
 * }</pre>
 *
 * <p>Inside the wrapped body the executing thread's MDC is temporarily replaced with the captured
 * snapshot, then restored (to whatever the thread had before) on exit — even on exception. Safe
 * to reuse across many wrap() calls: the snapshot is immutable at capture time.
 *
 * <p>{@link com.hitorro.util.concurrency.ParallelTasks} captures and wraps automatically, so most
 * callers of that helper do not need to reach for this class directly.
 */
public final class MdcContext {

    private final Map<String, String> snapshot;

    private MdcContext(Map<String, String> snapshot) {
        this.snapshot = snapshot;
    }

    /** Capture the current thread's MDC snapshot. Returns a context that can be re-applied elsewhere. */
    public static MdcContext capture() {
        return new MdcContext(MDC.getCopyOfContextMap());
    }

    /** Wrap a Runnable so it runs with the captured MDC installed, then restores the executing thread's prior MDC. */
    public Runnable wrap(Runnable body) {
        return () -> {
            Map<String, String> prev = MDC.getCopyOfContextMap();
            applySnapshot(snapshot);
            try {
                body.run();
            } finally {
                applySnapshot(prev);
            }
        };
    }

    /** Wrap a Callable, same semantics as {@link #wrap(Runnable)}. */
    public <T> Callable<T> wrap(Callable<T> body) {
        return () -> {
            Map<String, String> prev = MDC.getCopyOfContextMap();
            applySnapshot(snapshot);
            try {
                return body.call();
            } finally {
                applySnapshot(prev);
            }
        };
    }

    private static void applySnapshot(Map<String, String> m) {
        MDC.clear();
        if (m != null && !m.isEmpty()) {
            MDC.setContextMap(m);
        }
    }
}
