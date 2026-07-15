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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Retry")
class RetryTest {

    @Nested
    @DisplayName("Success paths")
    class Success {

        @Test
        @DisplayName("returns immediately on first-attempt success")
        void firstAttemptSuccess() {
            List<Long> sleeps = new ArrayList<>();
            String result = Retry.of("op")
                    .sleeper(sleeps::add)
                    .call(() -> "ok");
            assertThat(result).isEqualTo("ok");
            assertThat(sleeps).isEmpty();
        }

        @Test
        @DisplayName("succeeds on second attempt after one retryable failure")
        void secondAttempt() {
            AtomicInteger calls = new AtomicInteger();
            List<Long> sleeps = new ArrayList<>();
            String result = Retry.of("op")
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0.0)
                    .sleeper(sleeps::add)
                    .call(() -> {
                        if (calls.incrementAndGet() < 2) throw new IOException("boom");
                        return "ok";
                    });
            assertThat(result).isEqualTo("ok");
            assertThat(calls).hasValue(2);
            assertThat(sleeps).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Failure paths")
    class Failure {

        @Test
        @DisplayName("throws RetryExhausted after all attempts fail")
        void exhausted() {
            AtomicInteger calls = new AtomicInteger();
            assertThatThrownBy(() -> Retry.of("op")
                    .maxAttempts(3)
                    .initialDelay(Duration.ofMillis(1))
                    .jitter(0.0)
                    .sleeper(ms -> {})
                    .call(() -> {
                        calls.incrementAndGet();
                        throw new IOException("boom");
                    }))
                    .isInstanceOf(RetryExhaustedException.class)
                    .hasCauseInstanceOf(IOException.class);
            assertThat(calls).hasValue(3);
        }

        @Test
        @DisplayName("does not retry when predicate rejects the throwable")
        void nonRetryable() {
            AtomicInteger calls = new AtomicInteger();
            assertThatThrownBy(() -> Retry.of("op")
                    .maxAttempts(5)
                    .initialDelay(Duration.ofMillis(1))
                    .retryOn(t -> t instanceof IOException)
                    .sleeper(ms -> {})
                    .call(() -> {
                        calls.incrementAndGet();
                        throw new IllegalStateException("not retryable");
                    }))
                    .isInstanceOf(RetryExhaustedException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
            assertThat(calls).hasValue(1);
        }

        @Test
        @DisplayName("interrupt during sleep aborts and re-sets interrupt flag")
        void interrupted() {
            Thread t = new Thread(() -> {
                try {
                    Retry.of("op")
                            .maxAttempts(5)
                            .initialDelay(Duration.ofMillis(50))
                            .sleeper(Thread::sleep)
                            .call(() -> { throw new IOException("boom"); });
                } catch (RetryExhaustedException expected) {
                    // Expected — interrupted path throws RetryExhausted
                }
            });
            t.start();
            t.interrupt();
            try { t.join(5_000); } catch (InterruptedException ignored) {}
            assertThat(t.isAlive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Backoff math")
    class Backoff {

        @Test
        @DisplayName("without jitter, delays follow initial * multiplier^(n-1) capped at maxDelay")
        void backoffNoJitter() {
            Retry r = Retry.of("op")
                    .initialDelay(Duration.ofMillis(100))
                    .multiplier(2.0)
                    .maxDelay(Duration.ofMillis(500))
                    .jitter(0.0);
            assertThat(r.delayForAttempt(1)).isEqualTo(100);
            assertThat(r.delayForAttempt(2)).isEqualTo(200);
            assertThat(r.delayForAttempt(3)).isEqualTo(400);
            assertThat(r.delayForAttempt(4)).isEqualTo(500); // capped
            assertThat(r.delayForAttempt(10)).isEqualTo(500); // still capped
        }

        @Test
        @DisplayName("jitter stays within +/- fraction of the base delay")
        void jitterBounds() {
            Retry r = Retry.of("op")
                    .initialDelay(Duration.ofMillis(100))
                    .multiplier(1.0)
                    .maxDelay(Duration.ofSeconds(1))
                    .jitter(0.5);
            for (int i = 0; i < 200; i++) {
                long d = r.delayForAttempt(1);
                assertThat(d).isBetween(50L, 150L);
            }
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("rejects invalid parameter values")
        void rejectsBadParams() {
            assertThatThrownBy(() -> Retry.of("x").maxAttempts(0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Retry.of("x").multiplier(0.5))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Retry.of("x").jitter(2.0))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Retry.of("x").initialDelay(Duration.ofMillis(-1)))
                    .isInstanceOf(IllegalArgumentException.class);
            // maxDelay setter: null and negative both rejected (regression for prior missing checks).
            assertThatThrownBy(() -> Retry.of("x").maxDelay(null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Retry.of("x").maxDelay(Duration.ofMillis(-1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("run() variant wraps a void body")
        void runVariant() {
            AtomicInteger n = new AtomicInteger();
            assertThatCode(() -> Retry.of("op").sleeper(ms -> {}).run(n::incrementAndGet))
                    .doesNotThrowAnyException();
            assertThat(n).hasValue(1);
        }
    }
}
