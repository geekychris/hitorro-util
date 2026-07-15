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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

/**
 * Minimal span helper — measures wall time of a block and reports duration + outcome to a
 * pluggable sink. Not a real tracing implementation (no propagation, no exporters); use this
 * for lightweight timing observability. A future adapter could forward to OpenTelemetry.
 *
 * <pre>{@code
 * Tracing.span("indexer.commit", () -> {
 *     indexer.commit();
 * });
 * }</pre>
 */
public final class Tracing {

    private static final Logger LOG = LoggerFactory.getLogger(Tracing.class);

    /** (name, durationNanos, error-or-null) — swap this out to send spans elsewhere. */
    private static volatile BiConsumer<Span, Throwable> sink = (span, err) -> {
        if (err == null) LOG.debug("span {} ok {}ms", span.name, span.durationNanos / 1_000_000);
        else LOG.warn("span {} failed after {}ms: {}", span.name, span.durationNanos / 1_000_000, err.toString());
    };

    private Tracing() {}

    /** Replace the sink globally (e.g. to bridge into service counters). */
    public static void setSink(BiConsumer<Span, Throwable> newSink) {
        sink = newSink == null ? (s, e) -> {} : newSink;
    }

    public static void span(String name, Runnable body) {
        span(name, () -> { body.run(); return null; });
    }

    public static <T> T span(String name, Callable<T> body) {
        long start = System.nanoTime();
        Throwable err = null;
        try {
            return body.call();
        } catch (Throwable t) {
            err = t;
            if (t instanceof Error e) throw e;
            if (t instanceof RuntimeException re) throw re;
            throw new RuntimeException(t);
        } finally {
            long elapsed = System.nanoTime() - start;
            try {
                sink.accept(new Span(name, elapsed), err);
            } catch (RuntimeException reporterFailure) {
                LOG.warn("span sink threw {}", reporterFailure.toString());
            }
        }
    }

    public record Span(String name, long durationNanos) {
        public Duration duration() { return Duration.ofNanos(durationNanos); }
    }
}
