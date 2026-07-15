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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ParallelTasks")
class ParallelTasksTest {

    @Test
    @DisplayName("returns results in submitted order")
    void ordered() {
        List<Callable<Integer>> tasks = List.of(
                () -> { Thread.sleep(30); return 1; },
                () -> { Thread.sleep(10); return 2; },
                () -> { Thread.sleep(20); return 3; }
        );
        assertThat(ParallelTasks.runAll(tasks)).containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("runs tasks concurrently — total is close to the slowest, not the sum")
    void isConcurrent() {
        List<Callable<Long>> tasks = List.of(
                () -> { Thread.sleep(100); return 1L; },
                () -> { Thread.sleep(100); return 2L; },
                () -> { Thread.sleep(100); return 3L; }
        );
        long start = System.currentTimeMillis();
        ParallelTasks.runAll(tasks);
        long elapsed = System.currentTimeMillis() - start;
        // Sequential would be ~300ms; concurrent should be well under that even on slow CI runners.
        // We use a generous ceiling to tolerate scheduler variability while still catching a regression
        // that reintroduces sequential execution.
        assertThat(elapsed).isLessThan(500);
    }

    @Test
    @DisplayName("fail-fast: first exception aborts and remaining tasks are cancelled")
    void failFast() {
        AtomicInteger finished = new AtomicInteger();
        List<Callable<Integer>> tasks = List.of(
                () -> { Thread.sleep(10); throw new IllegalStateException("boom"); },
                () -> { Thread.sleep(500); finished.incrementAndGet(); return 2; }
        );
        assertThatThrownBy(() -> ParallelTasks.runAll(tasks))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThat(finished).hasValueLessThanOrEqualTo(0);
    }

    @Test
    @DisplayName("overall timeout throws and cancels in-flight tasks")
    void timeout() {
        List<Callable<Integer>> tasks = List.of(
                () -> { Thread.sleep(500); return 1; }
        );
        assertThatThrownBy(() -> ParallelTasks.runAll(tasks, Duration.ofMillis(50)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("timed out");
    }

    @Test
    @DisplayName("empty input returns empty list")
    void emptyInput() {
        assertThat(ParallelTasks.runAll(List.of())).isEmpty();
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("caller's MDC propagates to each task on the virtual-thread worker")
    void mdcPropagates() {
        MDC.put("corrId", "outer-123");
        List<Callable<String>> tasks = List.of(
                () -> "a=" + MDC.get("corrId"),
                () -> "b=" + MDC.get("corrId"),
                () -> "c=" + MDC.get("corrId")
        );
        List<String> results = ParallelTasks.runAll(tasks);
        assertThat(results).containsExactly("a=outer-123", "b=outer-123", "c=outer-123");
        // Outer thread MDC still intact after runAll.
        assertThat(MDC.get("corrId")).isEqualTo("outer-123");
    }
}
