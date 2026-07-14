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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SimpleScheduler + Scheduled scanner")
class SimpleSchedulerTest {

    private SimpleScheduler scheduler;

    @AfterEach
    void teardown() {
        if (scheduler != null) scheduler.stop();
    }

    @Test
    @DisplayName("every(): fixed-rate job fires multiple times")
    void every() throws InterruptedException {
        scheduler = new SimpleScheduler().start();
        CountDownLatch fired = new CountDownLatch(3);
        scheduler.every("tick", Duration.ofMillis(50), fired::countDown);
        assertThat(fired.await(3, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @DisplayName("cancel(): job stops firing")
    void cancel() throws InterruptedException {
        scheduler = new SimpleScheduler().start();
        AtomicInteger n = new AtomicInteger();
        JobKey key = scheduler.every("tick", Duration.ofMillis(50), n::incrementAndGet);
        Thread.sleep(180); // ~3-4 fires
        int atCancel = n.get();
        assertThat(scheduler.cancel(key)).isTrue();
        Thread.sleep(200);
        // Allow one straggler already-scheduled fire.
        assertThat(n.get()).isLessThanOrEqualTo(atCancel + 1);
    }

    @Test
    @DisplayName("scanner registers @Scheduled methods")
    void scannerRegistersMethods() throws InterruptedException {
        scheduler = new SimpleScheduler().start();
        SampleTarget target = new SampleTarget();
        List<JobKey> keys = ScheduledMethodScanner.registerAll(target, scheduler);
        assertThat(keys).hasSize(1);
        assertThat(target.tick.await(3, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    @DisplayName("scanner rejects methods with args")
    void scannerRejectsArgs() {
        scheduler = new SimpleScheduler().start();
        assertThatThrownBy(() -> ScheduledMethodScanner.registerAll(new BadTargetArgs(), scheduler))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("scanner rejects methods with neither cron nor fixedRate")
    void scannerRejectsNeither() {
        scheduler = new SimpleScheduler().start();
        assertThatThrownBy(() -> ScheduledMethodScanner.registerAll(new BadTargetNeither(), scheduler))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("CronExpressions validates well-formed and malformed strings")
    void cronValidation() {
        assertThat(CronExpressions.isValid(CronExpressions.EVERY_MINUTE)).isTrue();
        assertThat(CronExpressions.isValid("garbage")).isFalse();
        assertThatThrownBy(() -> CronExpressions.requireValid("junk"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class SampleTarget {
        final CountDownLatch tick = new CountDownLatch(2);

        @Scheduled(fixedRateMs = 50, name = "sample-tick")
        void tick() { tick.countDown(); }
    }

    static class BadTargetArgs {
        @Scheduled(fixedRateMs = 50)
        void takesArg(String x) { /* unused */ }
    }

    static class BadTargetNeither {
        @Scheduled
        void bad() { /* both cron and fixedRateMs unset */ }
    }
}
