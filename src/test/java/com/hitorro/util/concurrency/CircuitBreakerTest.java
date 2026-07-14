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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CircuitBreaker")
class CircuitBreakerTest {

    @Test
    @DisplayName("stays CLOSED while calls succeed")
    void closedOnSuccess() throws Exception {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("t", 3, Duration.ofSeconds(1), clock::get);
        for (int i = 0; i < 5; i++) cb.call(() -> "ok");
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("trips OPEN after consecutive failures reach the threshold")
    void tripsOpen() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("t", 3, Duration.ofSeconds(1), clock::get);
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> cb.call(() -> { throw new IOException("boom"); }))
                    .isInstanceOf(IOException.class);
        }
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("rejects immediately with CircuitOpenException when OPEN")
    void rejectsWhileOpen() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("t", 1, Duration.ofSeconds(10), clock::get);
        assertThatThrownBy(() -> cb.call(() -> { throw new IOException("boom"); }))
                .isInstanceOf(IOException.class);
        assertThatThrownBy(() -> cb.call(() -> "should not run"))
                .isInstanceOf(CircuitBreaker.CircuitOpenException.class);
    }

    @Test
    @DisplayName("moves to HALF_OPEN after cooldown; success closes it")
    void halfOpenThenClose() throws Exception {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("t", 1, Duration.ofMillis(100), clock::get);
        assertThatThrownBy(() -> cb.call(() -> { throw new IOException("boom"); }))
                .isInstanceOf(IOException.class);
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.OPEN);

        clock.addAndGet(150_000_000L); // 150ms
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.HALF_OPEN);

        String result = cb.call(() -> "ok");
        assertThat(result).isEqualTo("ok");
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("HALF_OPEN → OPEN if probe fails")
    void halfOpenProbeFails() {
        AtomicLong clock = new AtomicLong(0L);
        CircuitBreaker cb = new CircuitBreaker("t", 1, Duration.ofMillis(100), clock::get);
        assertThatThrownBy(() -> cb.call(() -> { throw new IOException("boom"); }))
                .isInstanceOf(IOException.class);

        clock.addAndGet(150_000_000L);
        assertThatThrownBy(() -> cb.call(() -> { throw new IOException("still bad"); }))
                .isInstanceOf(IOException.class);
        assertThat(cb.state()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
