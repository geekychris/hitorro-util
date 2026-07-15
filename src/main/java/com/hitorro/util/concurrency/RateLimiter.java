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

import java.time.Duration;
import java.util.function.LongSupplier;

/**
 * Token-bucket rate limiter. Thread-safe, allocation-free on the hot path.
 *
 * <pre>{@code
 * RateLimiter r = RateLimiter.perSecond(100);          // 100 ops/sec, burst 100
 * if (r.tryAcquire()) doWork();                         // non-blocking
 * r.acquire();                                          // blocks until a token is free
 * }</pre>
 */
public final class RateLimiter {

    private final double refillPerNano;
    private final double capacity;
    private final LongSupplier nanoClock;

    private double tokens;
    private long lastRefillNanos;

    public static RateLimiter perSecond(double permits) {
        return new RateLimiter(permits, permits, System::nanoTime);
    }

    public static RateLimiter perSecond(double permits, double burst) {
        return new RateLimiter(permits, burst, System::nanoTime);
    }

    /** Package-private for time-controlled tests. */
    RateLimiter(double permitsPerSecond, double burst, LongSupplier nanoClock) {
        if (permitsPerSecond <= 0) throw new IllegalArgumentException("permitsPerSecond must be > 0");
        if (burst <= 0) throw new IllegalArgumentException("burst must be > 0");
        this.refillPerNano = permitsPerSecond / 1_000_000_000.0;
        this.capacity = burst;
        this.nanoClock = nanoClock;
        this.tokens = burst;
        this.lastRefillNanos = nanoClock.getAsLong();
    }

    /** Try to acquire one token without blocking. */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    public synchronized boolean tryAcquire(int permits) {
        if (permits <= 0) throw new IllegalArgumentException("permits must be > 0");
        refill();
        if (tokens >= permits) {
            tokens -= permits;
            return true;
        }
        return false;
    }

    /** Block current thread until a token is available. */
    public void acquire() {
        acquire(1);
    }

    public void acquire(int permits) {
        if (permits <= 0) throw new IllegalArgumentException("permits must be > 0");
        while (true) {
            long waitNanos;
            synchronized (this) {
                refill();
                if (tokens >= permits) {
                    tokens -= permits;
                    return;
                }
                double deficit = permits - tokens;
                waitNanos = (long) Math.ceil(deficit / refillPerNano);
            }
            try {
                long ms = waitNanos / 1_000_000L;
                int ns = (int) (waitNanos % 1_000_000L);
                Thread.sleep(ms, ns);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("rate limiter acquire interrupted", e);
            }
        }
    }

    /** How many permits per second this limiter is set to. */
    public double permitsPerSecond() {
        return refillPerNano * 1_000_000_000.0;
    }

    /** Time it would take to accumulate {@code permits} tokens at full drain, for reporting. */
    public Duration estimatedWaitFor(int permits) {
        synchronized (this) {
            refill();
            if (tokens >= permits) return Duration.ZERO;
            double deficit = permits - tokens;
            return Duration.ofNanos((long) Math.ceil(deficit / refillPerNano));
        }
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        long elapsed = now - lastRefillNanos;
        if (elapsed <= 0) return;
        tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
        lastRefillNanos = now;
    }
}
