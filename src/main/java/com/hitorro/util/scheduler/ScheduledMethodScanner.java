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
package com.hitorro.util.scheduler;

import org.quartz.JobKey;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Registers all {@link Scheduled}-annotated no-arg methods on a target with a {@link SimpleScheduler}. */
public final class ScheduledMethodScanner {

    private ScheduledMethodScanner() {}

    public static List<JobKey> registerAll(Object target, SimpleScheduler scheduler) {
        List<JobKey> keys = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                Scheduled s = m.getAnnotation(Scheduled.class);
                if (s == null) continue;
                String dedupKey = m.getName() + Arrays.toString(m.getParameterTypes());
                if (!seen.add(dedupKey)) continue;
                if (m.getParameterCount() != 0) {
                    throw new IllegalStateException("@Scheduled methods must take no args: " + m);
                }
                m.setAccessible(true);
                String name = s.name().isEmpty()
                        ? target.getClass().getSimpleName() + "." + m.getName()
                        : s.name();
                Runnable body = wrap(target, m);

                boolean hasCron = !s.cron().isEmpty();
                boolean hasRate = s.fixedRateMs() > 0;
                if (hasCron == hasRate) {
                    throw new IllegalStateException(
                            "@Scheduled on " + m + " must specify exactly one of cron() or fixedRateMs()");
                }
                if (hasCron) {
                    keys.add(scheduler.schedule(name, s.cron(), body));
                } else {
                    keys.add(scheduler.every(name,
                            Duration.ofMillis(s.fixedRateMs()),
                            Duration.ofMillis(s.initialDelayMs()),
                            body));
                }
            }
        }
        return keys;
    }

    private static Runnable wrap(Object target, Method m) {
        return () -> {
            try {
                m.invoke(target);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause() != null ? e.getCause() : e;
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
            }
        };
    }
}
