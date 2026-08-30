/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Persisted definition of a recurring durable action. The scheduler
 * engine ({@link DurableScheduler}) reads these from a
 * {@link com.hitorro.util.persist.JsonFileEntityStore} on startup,
 * arms a cron or interval trigger for each enabled one, and stamps
 * the run-outcome fields back onto them after every fire.
 *
 * <p>Domain-specific extensions (e.g. "here's the YAML to run") are
 * carried on a subclass — Jackson serialises inherited public fields
 * without any extra configuration.</p>
 *
 * <p>Fields marked "engine-owned" are stamped by
 * {@link DurableScheduler} — callers should treat them as read-only.
 * Fields marked "operator-owned" are set at creation time and via
 * the CRUD API.</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DurableSchedule {

    // ---- operator-owned -----------------------------------------------

    /** Stable, human-readable id. Used in URLs, CLI, and as the checkpoint key. */
    public String name;

    public String description;

    /** Spring cron expression (6-field with seconds). Wins over intervalSeconds when both set. */
    public String cron;

    /** Fixed interval trigger. Ignored when {@link #cron} is set. */
    public Long intervalSeconds;

    /** Grace period past the interval before a boot catch-up fires.
     *  Prevents startup thrash when the process was down for < grace. */
    public long catchupGraceSeconds = 60;

    /** Max in-flight runs. 1 = strict serial (default). */
    public int maxConcurrent = 1;

    public boolean enabled = true;

    /** Opaque per-schedule offset the action's body substitutes into
     *  its own work. Engine advances it on success (or lets the action
     *  override via {@link ScheduleContext#setCheckpoint}). */
    public String checkpoint;

    // ---- engine-owned (do not set by hand) ----------------------------

    public Instant lastRunAt;
    public Instant lastSuccessAt;
    public Instant lastFailureAt;
    public String  lastError;
    public long    totalRuns = 0;
    public long    successfulRuns = 0;

    public Instant createdAt;
    public Instant updatedAt;
}
