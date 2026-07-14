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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tracing")
class TracingTest {

    private final List<Tracing.Span> spans = new ArrayList<>();
    private final AtomicReference<Throwable> lastErr = new AtomicReference<>();

    @org.junit.jupiter.api.BeforeEach
    void installSink() {
        Tracing.setSink((s, err) -> {
            spans.add(s);
            lastErr.set(err);
        });
    }

    @AfterEach
    void restoreDefault() {
        Tracing.setSink(null); // back to no-op; the default logger sink is fine for callers
    }

    @Test
    @DisplayName("returns the body's value and records success span")
    void success() {
        String result = Tracing.span("op", () -> "answer");
        assertThat(result).isEqualTo("answer");
        assertThat(spans).hasSize(1);
        assertThat(spans.get(0).name()).isEqualTo("op");
        assertThat(spans.get(0).durationNanos()).isPositive();
        assertThat(lastErr.get()).isNull();
    }

    @Test
    @DisplayName("records failure span and rethrows the exception")
    void failure() {
        assertThatThrownBy(() -> Tracing.span("op", () -> { throw new IllegalStateException("boom"); }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(spans).hasSize(1);
        assertThat(lastErr.get()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Runnable overload records a span")
    void runnableVariant() {
        Tracing.span("op", () -> { /* no-op */ });
        assertThat(spans).hasSize(1);
    }

    @Test
    @DisplayName("sink exception does not propagate to caller")
    void sinkFailureIsSwallowed() {
        Tracing.setSink((s, err) -> { throw new RuntimeException("sink broken"); });
        // Should not throw
        Tracing.span("op", () -> "ok");
    }
}
