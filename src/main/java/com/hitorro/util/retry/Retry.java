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
package com.hitorro.util.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

/**
 * Fluent retry-with-backoff helper.
 *
 * <pre>{@code
 * String body = Retry.of("fetch-config")
 *     .maxAttempts(5)
 *     .initialDelay(Duration.ofMillis(100))
 *     .multiplier(2.0)
 *     .maxDelay(Duration.ofSeconds(5))
 *     .jitter(0.2)
 *     .retryOn(t -> t instanceof java.io.IOException)
 *     .call(() -> http.get(url));
 * }</pre>
 *
 * Defaults: 3 attempts, 100ms initial, x2, cap 10s, 10% jitter, retry on any Exception (not Error).
 */
public final class Retry {

    private static final Logger LOG = LoggerFactory.getLogger(Retry.class);

    private final String name;
    private int maxAttempts = 3;
    private Duration initialDelay = Duration.ofMillis(100);
    private double multiplier = 2.0;
    private Duration maxDelay = Duration.ofSeconds(10);
    private double jitter = 0.1;
    private Predicate<Throwable> retryOn = t -> t instanceof Exception;
    private Sleeper sleeper = Thread::sleep;

    private Retry(String name) {
        this.name = name;
    }

    public static Retry of(String name) {
        return new Retry(name);
    }

    public Retry maxAttempts(int n) {
        if (n < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        this.maxAttempts = n;
        return this;
    }

    public Retry initialDelay(Duration d) {
        if (d.isNegative()) throw new IllegalArgumentException("initialDelay must be >= 0");
        this.initialDelay = d;
        return this;
    }

    public Retry multiplier(double m) {
        if (m < 1.0) throw new IllegalArgumentException("multiplier must be >= 1.0");
        this.multiplier = m;
        return this;
    }

    public Retry maxDelay(Duration d) {
        if (d == null) throw new IllegalArgumentException("maxDelay must not be null");
        if (d.isNegative()) throw new IllegalArgumentException("maxDelay must be >= 0");
        this.maxDelay = d;
        return this;
    }

    /** Jitter as fraction of the computed delay, e.g. 0.2 = +/-20%. */
    public Retry jitter(double fraction) {
        if (fraction < 0.0 || fraction > 1.0) throw new IllegalArgumentException("jitter must be in [0,1]");
        this.jitter = fraction;
        return this;
    }

    public Retry retryOn(Predicate<Throwable> pred) {
        this.retryOn = pred;
        return this;
    }

    /** Package-private hook for tests to avoid real sleeping. */
    Retry sleeper(Sleeper s) {
        this.sleeper = s;
        return this;
    }

    public <T> T call(Callable<T> body) {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return body.call();
            } catch (Exception t) {
                last = t;
                if (attempt == maxAttempts || !retryOn.test(t)) {
                    break;
                }
                long delayMs = delayForAttempt(attempt);
                LOG.debug("retry[{}] attempt {}/{} failed ({}), sleeping {}ms",
                        name, attempt, maxAttempts, t.toString(), delayMs);
                try {
                    sleeper.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RetryExhaustedException(
                            "retry[" + name + "] interrupted after " + attempt + " attempts", t);
                }
            }
        }
        throw new RetryExhaustedException(
                "retry[" + name + "] exhausted after " + maxAttempts + " attempts", last);
    }

    public void run(RunnableThrows body) {
        call(() -> { body.run(); return null; });
    }

    long delayForAttempt(int attempt) {
        double base = initialDelay.toMillis() * Math.pow(multiplier, attempt - 1);
        double capped = Math.min(base, maxDelay.toMillis());
        if (jitter > 0.0) {
            double range = capped * jitter;
            double offset = (ThreadLocalRandom.current().nextDouble() * 2.0 - 1.0) * range;
            capped = Math.max(0.0, capped + offset);
        }
        return (long) capped;
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    @FunctionalInterface
    public interface RunnableThrows {
        void run() throws Exception;
    }
}
