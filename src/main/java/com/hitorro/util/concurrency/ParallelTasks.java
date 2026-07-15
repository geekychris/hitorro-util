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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fan-out helper that runs a collection of tasks on virtual threads and collects results in order.
 *
 * <p>Semantics chosen to match a subset of {@code StructuredTaskScope.ShutdownOnFailure} — which is
 * still preview in Java 21 — without requiring {@code --enable-preview}. On first task failure, the
 * remaining tasks are cancelled and the failure is rethrown wrapped in {@link RuntimeException}.
 *
 * <p>This is not a general-purpose executor: the caller supplies a bounded task list, waits
 * synchronously for all of them, and gets an in-order result list back.
 */
public final class ParallelTasks {

    private ParallelTasks() {}

    /** Run all tasks concurrently, return results in the same order. Fail-fast on first exception. */
    public static <T> List<T> runAll(List<Callable<T>> tasks) {
        return runAll(tasks, null);
    }

    /**
     * Run all tasks concurrently with an overall deadline. On timeout, in-flight tasks are cancelled
     * and a {@link RuntimeException} wrapping {@link TimeoutException} is thrown.
     */
    public static <T> List<T> runAll(List<Callable<T>> tasks, Duration timeout) {
        if (tasks.isEmpty()) return List.of();
        try (ExecutorService exec = VirtualExecutors.virtualPerTask("parallel")) {
            ExecutorCompletionService<T> completion = new ExecutorCompletionService<>(exec);
            List<Future<T>> futures = new ArrayList<>(tasks.size());
            for (Callable<T> t : tasks) futures.add(completion.submit(t));

            long deadlineNanos = timeout == null ? 0L : System.nanoTime() + timeout.toNanos();
            try {
                // Observe tasks in completion order so a fast failure is surfaced immediately,
                // rather than waiting on earlier-submitted long-running tasks.
                for (int i = 0; i < futures.size(); i++) {
                    Future<T> done;
                    if (timeout == null) {
                        done = completion.take();
                    } else {
                        long remaining = deadlineNanos - System.nanoTime();
                        if (remaining <= 0) throw new TimeoutException();
                        done = completion.poll(remaining, TimeUnit.NANOSECONDS);
                        if (done == null) throw new TimeoutException();
                    }
                    // Trigger ExecutionException here if this task failed; result value discarded.
                    done.get();
                }
                // All tasks succeeded — collect results in original submission order.
                List<T> results = new ArrayList<>(futures.size());
                for (Future<T> f : futures) results.add(f.get());
                return results;
            } catch (ExecutionException e) {
                cancelAll(futures);
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException re) throw re;
                throw new CompletionException(cause);
            } catch (TimeoutException te) {
                cancelAll(futures);
                throw new RuntimeException("parallel tasks timed out after " + timeout, te);
            } catch (InterruptedException ie) {
                cancelAll(futures);
                Thread.currentThread().interrupt();
                throw new RuntimeException("parallel tasks interrupted", ie);
            }
        }
    }

    private static void cancelAll(List<? extends Future<?>> futures) {
        for (Future<?> f : futures) f.cancel(true);
    }
}
