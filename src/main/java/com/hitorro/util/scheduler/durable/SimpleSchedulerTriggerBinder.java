/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

import com.hitorro.util.scheduler.SimpleScheduler;
import org.quartz.JobKey;

import java.time.Duration;

/**
 * {@link TriggerBinder} backed by hitorro's {@link SimpleScheduler}
 * (which wraps Quartz). Ready-to-use default for services that don't
 * already have a Spring {@code TaskScheduler} bean.
 *
 * <p>The caller supplies (and manages the lifecycle of) the
 * SimpleScheduler instance — this class doesn't call
 * {@code start()} or {@code stop()} on it.</p>
 */
public final class SimpleSchedulerTriggerBinder implements TriggerBinder {

    private final SimpleScheduler scheduler;

    public SimpleSchedulerTriggerBinder(SimpleScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public CancelHandle bindCron(String name, String cronExpr, Runnable body) {
        JobKey key = scheduler.schedule(name, cronExpr, body);
        return () -> scheduler.cancel(key);
    }

    @Override
    public CancelHandle bindInterval(String name, long seconds, Runnable body) {
        JobKey key = scheduler.every(name, Duration.ofSeconds(Math.max(1, seconds)), body);
        return () -> scheduler.cancel(key);
    }
}
