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
import java.util.concurrent.Callable;
import java.util.function.LongSupplier;

/**
 * Circuit breaker with CLOSED / OPEN / HALF_OPEN states.
 *
 * <p>State transitions:
 * <ul>
 *   <li>CLOSED → OPEN when consecutive failures reach {@code failureThreshold}.</li>
 *   <li>OPEN → HALF_OPEN after {@code cooldown} elapses; the next call is admitted as a probe.</li>
 *   <li>HALF_OPEN → CLOSED on probe success; HALF_OPEN → OPEN on probe failure.</li>
 * </ul>
 *
 * <p>When OPEN, {@link #call} throws {@link CircuitOpenException} immediately without invoking
 * the body.
 */
public final class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String name;
    private final int failureThreshold;
    private final long cooldownNanos;
    private final LongSupplier nanoClock;

    private volatile State state = State.CLOSED;
    private int consecutiveFailures = 0;
    private long openedAtNanos = 0L;
    private boolean probeInFlight = false;

    public CircuitBreaker(String name, int failureThreshold, Duration cooldown) {
        this(name, failureThreshold, cooldown, System::nanoTime);
    }

    CircuitBreaker(String name, int failureThreshold, Duration cooldown, LongSupplier nanoClock) {
        if (failureThreshold < 1) throw new IllegalArgumentException("failureThreshold must be >= 1");
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.cooldownNanos = cooldown.toNanos();
        this.nanoClock = nanoClock;
    }

    public State state() {
        maybeHalfOpen();
        return state;
    }

    public <T> T call(Callable<T> body) throws Exception {
        boolean probeClaimed = false;
        synchronized (this) {
            maybeHalfOpen();
            if (state == State.OPEN) {
                throw new CircuitOpenException("circuit[" + name + "] is OPEN");
            }
            if (state == State.HALF_OPEN) {
                if (probeInFlight) {
                    throw new CircuitOpenException("circuit[" + name + "] is OPEN");
                }
                probeInFlight = true;
                probeClaimed = true;
            }
        }
        try {
            T result = body.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        } finally {
            if (probeClaimed) {
                synchronized (this) {
                    probeInFlight = false;
                }
            }
        }
    }

    private synchronized void onSuccess() {
        consecutiveFailures = 0;
        state = State.CLOSED;
    }

    private synchronized void onFailure() {
        consecutiveFailures++;
        if (consecutiveFailures >= failureThreshold) {
            state = State.OPEN;
            openedAtNanos = nanoClock.getAsLong();
        }
    }

    private synchronized void maybeHalfOpen() {
        if (state == State.OPEN && nanoClock.getAsLong() - openedAtNanos >= cooldownNanos) {
            state = State.HALF_OPEN;
        }
    }

    public static final class CircuitOpenException extends RuntimeException {
        public CircuitOpenException(String message) { super(message); }
    }
}
