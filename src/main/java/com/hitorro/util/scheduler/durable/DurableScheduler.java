/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

import com.hitorro.util.persist.JsonFileEntityStore;
import com.hitorro.util.persist.NamedTextValueStore;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Cron/interval-triggered actions with durable checkpoints and
 * boot-time catch-up. The generic engine underneath mesh's pipeline
 * scheduler.
 *
 * <h3>Contract</h3>
 * <ul>
 *   <li><b>Persistent definitions.</b> Schedules survive process
 *       restart via a caller-supplied {@link JsonFileEntityStore}.</li>
 *   <li><b>Persistent checkpoints.</b> Each schedule owns an opaque
 *       string value (its "last good key") stored in a
 *       {@link NamedTextValueStore}. The engine advances it after
 *       every successful run.</li>
 *   <li><b>Boot catch-up.</b> On {@link #bootstrap}, schedules whose
 *       last success is older than one interval fire one immediate
 *       make-up run — bounded by {@code catchupGraceSeconds}.</li>
 *   <li><b>maxConcurrent gating.</b> Per-schedule cap on in-flight
 *       runs. Overflow ticks are dropped.</li>
 *   <li><b>Checkpoint override.</b> On success the engine stamps
 *       {@code ${NOW}} as the new checkpoint UNLESS the action
 *       called {@link ScheduleContext#setCheckpoint} — that value
 *       wins.</li>
 * </ul>
 *
 * <h3>What this engine does NOT do</h3>
 * <ul>
 *   <li>Multi-process leader election. One JVM writing per store dir.</li>
 *   <li>Sub-second cadences (bound by the underlying
 *       {@link TriggerBinder}'s resolution).</li>
 * </ul>
 *
 * @param <S> concrete schedule type (must extend {@link DurableSchedule});
 *            callers add domain-specific fields on a subclass
 */
public class DurableScheduler<S extends DurableSchedule> {

    private final JsonFileEntityStore<S> store;
    private final NamedTextValueStore checkpoints;
    private final TriggerBinder binder;
    private final Consumer<ScheduleContext<S>> action;
    private final ConcurrentHashMap<String, TriggerBinder.CancelHandle> handles = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> inflight = new ConcurrentHashMap<>();

    /**
     * @param store        durable per-JVM schedule registry
     * @param checkpoints  durable per-schedule "last good key" store
     * @param binder       how to actually get callbacks on time
     * @param action       what to do when a schedule fires; throw to signal failure
     */
    public DurableScheduler(JsonFileEntityStore<S> store,
                            NamedTextValueStore checkpoints,
                            TriggerBinder binder,
                            Consumer<ScheduleContext<S>> action) {
        this.store = store;
        this.checkpoints = checkpoints;
        this.binder = binder;
        this.action = action;
    }

    /** Load persisted schedules, fire catch-up runs where warranted,
     *  arm triggers for enabled ones. Call once at service startup. */
    public void bootstrap() {
        for (S s : store.all()) {
            hydrateCheckpoint(s);
            if (shouldCatchUp(s)) trigger(s, /*catchup*/ true);
            if (s.enabled) armTrigger(s);
        }
    }

    /** Unbind every trigger. Call at service shutdown. */
    public void shutdown() {
        for (TriggerBinder.CancelHandle h : handles.values()) h.cancel();
        handles.clear();
    }

    // ---- CRUD ---------------------------------------------------------

    public List<S> list() {
        List<S> out = store.all();
        for (S s : out) hydrateCheckpoint(s);
        return out;
    }

    public S get(String name) {
        S s = store.get(name).orElseThrow(() -> new IllegalArgumentException("no schedule: " + name));
        hydrateCheckpoint(s);
        return s;
    }

    public S save(S schedule) throws IOException {
        Instant now = Instant.now();
        if (schedule.createdAt == null) schedule.createdAt = now;
        schedule.updatedAt = now;
        S saved = store.put(schedule);
        if (schedule.checkpoint != null) checkpoints.put(schedule.name, schedule.checkpoint);
        rebindTrigger(saved);
        return saved;
    }

    public boolean delete(String name) throws IOException {
        TriggerBinder.CancelHandle h = handles.remove(name);
        if (h != null) h.cancel();
        boolean removed = store.remove(name);
        checkpoints.remove(name);
        return removed;
    }

    public S setEnabled(String name, boolean enabled) throws IOException {
        S s = store.update(name, x -> {
            x.enabled = enabled;
            x.updatedAt = Instant.now();
        });
        rebindTrigger(s);
        return s;
    }

    public String getCheckpoint(String name) {
        return checkpoints.get(name).orElse("");
    }

    public void setCheckpoint(String name, String value) throws IOException {
        store.update(name, s -> {
            s.checkpoint = value;
            s.updatedAt = Instant.now();
        });
        checkpoints.put(name, value == null ? "" : value);
    }

    /** Trigger immediately. Honours {@code maxConcurrent}; returns
     *  false when the in-flight cap was already hit. */
    public boolean runNow(String name) {
        S s = get(name);
        return trigger(s, /*catchup*/ false) != null;
    }

    // ---- trigger management ------------------------------------------

    private void rebindTrigger(S s) {
        TriggerBinder.CancelHandle old = handles.remove(s.name);
        if (old != null) old.cancel();
        if (s.enabled) armTrigger(s);
    }

    private void armTrigger(S s) {
        Runnable body = () -> {
            try { trigger(s, /*catchup*/ false); }
            catch (Throwable t) { /* trigger already caught + recorded — swallow so Quartz doesn't misfire the schedule */ }
        };
        TriggerBinder.CancelHandle h;
        if (s.cron != null && !s.cron.isBlank()) {
            h = binder.bindCron(s.name, s.cron, body);
        } else if (s.intervalSeconds != null && s.intervalSeconds > 0) {
            h = binder.bindInterval(s.name, s.intervalSeconds, body);
        } else {
            return;   // manual-only schedule; run-now still works
        }
        handles.put(s.name, h);
    }

    /** Fire once. Returns the ScheduleContext used, or null when the
     *  in-flight cap was hit. Public for tests + run-now. */
    protected ScheduleContext<S> trigger(S s, boolean catchup) {
        AtomicInteger n = inflight.computeIfAbsent(s.name, k -> new AtomicInteger());
        if (n.get() >= Math.max(1, s.maxConcurrent)) return null;
        n.incrementAndGet();
        try { return doTrigger(s, catchup); }
        finally { n.decrementAndGet(); }
    }

    private ScheduleContext<S> doTrigger(S s, boolean catchup) {
        Instant now = Instant.now();
        String checkpoint = checkpoints.get(s.name).orElse(s.checkpoint == null ? "" : s.checkpoint);
        ScheduleContext<S> ctx = new ScheduleContext<>(s, now, catchup, checkpoint);
        try {
            action.accept(ctx);
            recordSuccess(s, now, ctx);
        } catch (Throwable t) {
            recordFailure(s, now, t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
        }
        return ctx;
    }

    private void recordSuccess(S s, Instant runStart, ScheduleContext<S> ctx) {
        try {
            store.update(s.name, x -> {
                x.lastRunAt = runStart;
                x.lastSuccessAt = runStart;
                x.successfulRuns++;
                x.totalRuns++;
                x.lastError = null;
                x.updatedAt = runStart;
            });
            // Precedence: action override → external checkpoint PUT → ${NOW}
            String currentDisk = checkpoints.get(s.name).orElse("");
            Optional<S> fresh = store.get(s.name);
            boolean externallyChanged = fresh.isPresent()
                    && fresh.get().checkpoint != null
                    && !fresh.get().checkpoint.equals(currentDisk);
            String override = ctx.checkpointOverride();
            if (override != null) {
                checkpoints.put(s.name, override);
                store.update(s.name, x -> x.checkpoint = override);
            } else if (!externallyChanged) {
                String advance = runStart.toString();
                checkpoints.put(s.name, advance);
                store.update(s.name, x -> x.checkpoint = advance);
            }
        } catch (IOException e) {
            // Persistence failure — surface via stderr, don't propagate
            // (the run itself already succeeded from the action's POV).
            System.err.println("[DurableScheduler] persist success failed: " + e.getMessage());
        }
    }

    private void recordFailure(S s, Instant when, String err) {
        try {
            store.update(s.name, x -> {
                x.lastRunAt = when;
                x.lastFailureAt = when;
                x.lastError = err;
                x.totalRuns++;
                x.updatedAt = when;
            });
        } catch (IOException e) {
            System.err.println("[DurableScheduler] persist failure failed: " + e.getMessage());
        }
    }

    private boolean shouldCatchUp(S s) {
        if (!s.enabled) return false;
        if (s.lastSuccessAt == null) return false;
        long intervalSec = s.intervalSeconds != null ? s.intervalSeconds : 3600;
        long gap = Instant.now().getEpochSecond() - s.lastSuccessAt.getEpochSecond();
        return gap > (intervalSec + Math.max(0, s.catchupGraceSeconds));
    }

    private void hydrateCheckpoint(S s) {
        checkpoints.get(s.name).ifPresent(v -> s.checkpoint = v);
    }
}
