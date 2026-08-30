/*
 * Copyright (c) 2006-2026 Chris Collins
 */
package com.hitorro.util.scheduler.durable;

import com.hitorro.util.persist.JsonFileEntityStore;
import com.hitorro.util.persist.NamedTextValueStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Direct engine tests — bypass the trigger binder by driving
 * {@link DurableScheduler#runNow} manually so tests are deterministic.
 */
class DurableSchedulerTest {

    private DurableScheduler<DurableSchedule> engine(Path tmp,
            java.util.function.Consumer<ScheduleContext<DurableSchedule>> action) throws IOException {
        JsonFileEntityStore<DurableSchedule> store = new JsonFileEntityStore<>(
                tmp.resolve("s.json"), DurableSchedule.class, s -> s.name);
        NamedTextValueStore cp = new NamedTextValueStore(tmp.resolve("cp"));
        TriggerBinder noopBinder = new TriggerBinder() {
            @Override public CancelHandle bindCron(String n, String c, Runnable body) { return () -> {}; }
            @Override public CancelHandle bindInterval(String n, long s, Runnable body) { return () -> {}; }
        };
        return new DurableScheduler<>(store, cp, noopBinder, action);
    }

    private DurableSchedule sched(String name) {
        DurableSchedule s = new DurableSchedule();
        s.name = name;
        s.intervalSeconds = 60L;
        s.enabled = true;
        s.maxConcurrent = 1;
        return s;
    }

    @Test
    void successful_run_advances_checkpoint_to_NOW(@TempDir Path tmp) throws IOException {
        AtomicInteger runs = new AtomicInteger();
        var e = engine(tmp, ctx -> runs.incrementAndGet());
        e.save(sched("s"));

        assertThat(e.runNow("s")).isTrue();
        assertThat(runs.get()).isEqualTo(1);

        String cp = e.getCheckpoint("s");
        assertThat(cp).isNotEmpty();
        assertThat(java.time.Instant.parse(cp)).isNotNull();  // must be an ISO instant

        DurableSchedule after = e.get("s");
        assertThat(after.totalRuns).isEqualTo(1);
        assertThat(after.successfulRuns).isEqualTo(1);
        assertThat(after.lastError).isNull();
    }

    @Test
    void action_setCheckpoint_wins_over_NOW(@TempDir Path tmp) throws IOException {
        var e = engine(tmp, ctx -> ctx.setCheckpoint("custom-value-42"));
        e.save(sched("s"));

        e.runNow("s");
        assertThat(e.getCheckpoint("s")).isEqualTo("custom-value-42");
    }

    @Test
    void action_throwing_records_failure_without_advancing_checkpoint(@TempDir Path tmp) throws IOException {
        var e = engine(tmp, ctx -> { throw new RuntimeException("boom"); });
        e.save(sched("s"));

        e.runNow("s");
        assertThat(e.getCheckpoint("s")).isEmpty();
        DurableSchedule after = e.get("s");
        assertThat(after.totalRuns).isEqualTo(1);
        assertThat(after.successfulRuns).isEqualTo(0);
        assertThat(after.lastError).contains("boom");
        assertThat(after.lastFailureAt).isNotNull();
    }

    @Test
    void run_reads_current_checkpoint(@TempDir Path tmp) throws IOException {
        List<String> observed = new ArrayList<>();
        var e = engine(tmp, ctx -> observed.add(ctx.checkpoint()));
        DurableSchedule s = sched("s");
        s.checkpoint = "start-here";
        e.save(s);

        e.runNow("s");
        assertThat(observed).containsExactly("start-here");
    }

    @Test
    void maxConcurrent_blocks_reentrant_run(@TempDir Path tmp) throws IOException {
        // Reentry from inside the action: action calls runNow again on the
        // same schedule while it's still executing. The inner call must
        // be rejected (returns false) because the counter is already at 1.
        List<Boolean> innerAccepted = new ArrayList<>();
        DurableScheduler<DurableSchedule>[] engineRef = new DurableScheduler[1];
        var e = engine(tmp, ctx -> innerAccepted.add(engineRef[0].runNow("s")));
        engineRef[0] = e;
        DurableSchedule s = sched("s");
        s.maxConcurrent = 1;
        e.save(s);

        assertThat(e.runNow("s")).isTrue();
        assertThat(innerAccepted).containsExactly(false);   // inner call blocked by the cap
    }

    @Test
    void save_then_delete_removes_checkpoint_and_definition(@TempDir Path tmp) throws IOException {
        var e = engine(tmp, ctx -> ctx.setCheckpoint("v"));
        e.save(sched("s"));
        e.runNow("s");
        assertThat(e.getCheckpoint("s")).isEqualTo("v");

        assertThat(e.delete("s")).isTrue();
        try { e.get("s"); org.assertj.core.api.Assertions.fail("expected"); }
        catch (IllegalArgumentException ok) { }
        assertThat(e.getCheckpoint("s")).isEmpty();
    }
}
