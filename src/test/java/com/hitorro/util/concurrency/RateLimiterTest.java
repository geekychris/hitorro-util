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

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RateLimiter")
class RateLimiterTest {

    @Test
    @DisplayName("burst tokens are immediately available")
    void burstAvailable() {
        AtomicLong clock = new AtomicLong(0L);
        RateLimiter r = new RateLimiter(10.0, 5.0, clock::get);
        for (int i = 0; i < 5; i++) assertThat(r.tryAcquire()).isTrue();
        assertThat(r.tryAcquire()).isFalse(); // exhausted
    }

    @Test
    @DisplayName("tokens refill at configured rate")
    void refillsOverTime() {
        AtomicLong clock = new AtomicLong(0L);
        RateLimiter r = new RateLimiter(10.0, 1.0, clock::get); // 10 per second
        assertThat(r.tryAcquire()).isTrue();
        assertThat(r.tryAcquire()).isFalse();

        // Advance clock 100ms → 1 token refilled
        clock.addAndGet(100_000_000L);
        assertThat(r.tryAcquire()).isTrue();
        assertThat(r.tryAcquire()).isFalse();
    }

    @Test
    @DisplayName("refill is capped at burst capacity")
    void refillCapped() {
        AtomicLong clock = new AtomicLong(0L);
        RateLimiter r = new RateLimiter(10.0, 3.0, clock::get);
        for (int i = 0; i < 3; i++) r.tryAcquire();

        // Advance clock 10s → would produce 100 tokens, but cap is 3
        clock.addAndGet(10_000_000_000L);
        for (int i = 0; i < 3; i++) assertThat(r.tryAcquire()).isTrue();
        assertThat(r.tryAcquire()).isFalse();
    }

    @Test
    @DisplayName("estimatedWaitFor returns non-zero when starved")
    void estimatedWait() {
        AtomicLong clock = new AtomicLong(0L);
        RateLimiter r = new RateLimiter(10.0, 1.0, clock::get);
        r.tryAcquire();
        assertThat(r.estimatedWaitFor(1)).isEqualTo(Duration.ofMillis(100));
    }

    @Test
    @DisplayName("rejects invalid construction args")
    void badArgs() {
        assertThatThrownBy(() -> RateLimiter.perSecond(0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RateLimiter.perSecond(10.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("permitsPerSecond returns the configured rate")
    void rateReadback() {
        RateLimiter r = RateLimiter.perSecond(42.0);
        assertThat(r.permitsPerSecond()).isCloseTo(42.0, org.assertj.core.data.Offset.offset(1e-9));
    }
}
