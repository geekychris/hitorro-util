/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

import java.time.Instant;

/**
 * Per-run handle passed to the action body. Carries the schedule
 * being fired, the run's start time, whether this is a boot-time
 * catch-up run, and the current checkpoint value.
 *
 * <p>The action calls {@link #setCheckpoint} to override the
 * scheduler's default {@code ${NOW}} advance — needed for
 * max-row-id / opaque cursors where only the action knows the value
 * it consumed up to.</p>
 *
 * <p>Marking the run as failed is done by throwing from the action
 * body — the scheduler catches, records {@code lastError}, and does
 * NOT advance the checkpoint.</p>
 */
public final class ScheduleContext<S extends DurableSchedule> {

    private final S schedule;
    private final Instant now;
    private final boolean catchup;
    private final String checkpoint;
    private volatile String checkpointOverride;

    public ScheduleContext(S schedule, Instant now, boolean catchup, String checkpoint) {
        this.schedule = schedule;
        this.now = now;
        this.catchup = catchup;
        this.checkpoint = checkpoint;
    }

    public S schedule() { return schedule; }
    public Instant now() { return now; }
    public boolean isCatchup() { return catchup; }

    /** Current checkpoint the run was invoked with. Never null (blank
     *  string when never set — treat both the same in your action body). */
    public String checkpoint() { return checkpoint == null ? "" : checkpoint; }

    /** Read-only view of the override (null until an action calls setCheckpoint). */
    public String checkpointOverride() { return checkpointOverride; }

    /** Record a checkpoint value the scheduler should persist as the
     *  next run's {@code ${CHECKPOINT}}. Last write wins if called
     *  more than once in one run. */
    public void setCheckpoint(String value) { this.checkpointOverride = value; }
}
