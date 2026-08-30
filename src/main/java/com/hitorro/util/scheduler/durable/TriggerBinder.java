/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

/**
 * Abstraction over "get a callback on time." Two impls: Spring's
 * {@code TaskScheduler} (in the mesh driver-app), and hitorro's
 * {@code SimpleScheduler} / Quartz (in this package, see
 * {@link SimpleSchedulerTriggerBinder}). Keeping the interface here
 * means {@link DurableScheduler} doesn't need to pull either dep
 * into its own compile classpath.
 *
 * <p>Both impls guarantee that the {@code body} runnable is invoked
 * on a worker thread — never on the caller's thread or a shared
 * dispatch thread — so long-running actions won't block subsequent
 * fires of other schedules.</p>
 */
public interface TriggerBinder {

    /** Arms a cron-triggered callback. The returned handle unschedules
     *  it when called; safe to call twice. */
    CancelHandle bindCron(String name, String cronExpr, Runnable body);

    /** Arms a fixed-interval callback. First fire happens after {@code seconds}. */
    CancelHandle bindInterval(String name, long seconds, Runnable body);

    /** Opaque handle for unbinding. Idempotent. */
    interface CancelHandle {
        void cancel();
    }
}
