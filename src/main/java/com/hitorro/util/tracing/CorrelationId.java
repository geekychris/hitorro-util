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

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Thread-local correlation ID stored in SLF4J's MDC under key {@link #MDC_KEY}.
 *
 * <p>Logback/log4j layout patterns that include {@code %X{corrId}} will pick it up automatically.
 * For inbound requests, extract from a header (e.g. {@code X-Correlation-Id}) and call
 * {@link #set(String)}; for outbound calls, propagate {@link #get()} on the wire.
 */
public final class CorrelationId {

    public static final String MDC_KEY = "corrId";

    private CorrelationId() {}

    /** Returns the current correlation ID, or {@code null} if none set. */
    public static String get() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String id) {
        if (id == null) {
            MDC.remove(MDC_KEY);
            return;
        }
        if (id.isEmpty()) {
            throw new IllegalArgumentException("correlation id must not be empty");
        }
        if (id.length() > 128) {
            throw new IllegalArgumentException("correlation id exceeds max length of 128");
        }
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                throw new IllegalArgumentException("correlation id contains control character");
            }
        }
        MDC.put(MDC_KEY, id);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }

    /** Generate and set a fresh random ID (short UUID form). Returns the value written. */
    public static String generate() {
        String id = UUID.randomUUID().toString().substring(0, 12);
        set(id);
        return id;
    }

    /**
     * Runs {@code body} with the given correlation ID installed. The previous value (if any) is
     * restored on exit — safe to nest.
     */
    public static void with(String id, Runnable body) {
        String prev = get();
        set(id);
        try {
            body.run();
        } finally {
            set(prev);
        }
    }

    public static <T> T with(String id, Callable<T> body) throws Exception {
        String prev = get();
        set(id);
        try {
            return body.call();
        } finally {
            set(prev);
        }
    }
}
