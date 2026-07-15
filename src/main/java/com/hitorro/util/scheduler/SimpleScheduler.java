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

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.time.Duration;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Fluent, small-footprint scheduler over Quartz. Registers Runnables (or {@link Scheduled}-annotated
 * targets via {@link ScheduledMethodScanner}) without requiring callers to hand-write JobDetail /
 * Trigger boilerplate.
 *
 * <pre>{@code
 * SimpleScheduler s = new SimpleScheduler().start();
 * s.schedule("reindex", CronExpressions.EVERY_HOUR, () -> indexer.reindex());
 * s.every("gc-tmp", Duration.ofMinutes(5), () -> tmpDir.cleanup());
 * s.stop();
 * }</pre>
 */
public class SimpleScheduler {

    /** Quartz misfire behaviour for cron triggers. */
    public enum Misfire {
        /** Fire once immediately, then continue. */
        FIRE_ONCE_NOW,
        /** Skip missed fires; wait for the next scheduled fire. */
        DO_NOTHING,
        /** Quartz smart policy (default). */
        SMART
    }

    private static final String BODY_KEY = "hitorro.body.id";

    private final ConcurrentMap<String, Runnable> bodies = new ConcurrentHashMap<>();
    private final Scheduler scheduler;
    private volatile boolean started = false;
    private Misfire misfire = Misfire.SMART;

    public SimpleScheduler() {
        try {
            Properties props = new Properties();
            String instanceName = "hitorro-simple-" + UUID.randomUUID();
            props.setProperty("org.quartz.scheduler.instanceName", instanceName);
            props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
            props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            props.setProperty("org.quartz.threadPool.threadCount", "10");
            props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            this.scheduler = factory.getScheduler();
            this.scheduler.setJobFactory(new BodyResolvingJobFactory(bodies));
        } catch (SchedulerException e) {
            throw new RuntimeException("failed to create Quartz scheduler", e);
        }
    }

    /** Package-private for tests. */
    SimpleScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        try {
            this.scheduler.setJobFactory(new BodyResolvingJobFactory(bodies));
        } catch (SchedulerException e) {
            throw new RuntimeException("failed to install job factory", e);
        }
    }

    public SimpleScheduler misfire(Misfire policy) {
        this.misfire = policy;
        return this;
    }

    public synchronized SimpleScheduler start() {
        if (!started) {
            try {
                scheduler.start();
                started = true;
            } catch (SchedulerException e) {
                throw new RuntimeException("failed to start scheduler", e);
            }
        }
        return this;
    }

    public synchronized void stop() {
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException e) {
            throw new RuntimeException("failed to stop scheduler", e);
        }
        bodies.clear();
    }

    /** Register a job that fires on the given cron expression. */
    public JobKey schedule(String name, String cron, Runnable body) {
        CronExpressions.requireValid(cron);
        String bodyId = registerBody(body);
        JobDetail job = buildJob(name, bodyId);
        CronScheduleBuilder cb = CronScheduleBuilder.cronSchedule(cron);
        switch (misfire) {
            case FIRE_ONCE_NOW -> cb = cb.withMisfireHandlingInstructionFireAndProceed();
            case DO_NOTHING    -> cb = cb.withMisfireHandlingInstructionDoNothing();
            case SMART         -> { /* default */ }
        }
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trg-" + name, "hitorro")
                .withSchedule(cb)
                .startNow()
                .build();
        submit(job, trigger);
        return job.getKey();
    }

    /** Register a fixed-rate job (fires every {@code period}, starting after {@code initialDelay}). */
    public JobKey every(String name, Duration period, Runnable body) {
        return every(name, period, Duration.ZERO, body);
    }

    public JobKey every(String name, Duration period, Duration initialDelay, Runnable body) {
        String bodyId = registerBody(body);
        JobDetail job = buildJob(name, bodyId);
        SimpleScheduleBuilder sb = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(period.toMillis())
                .repeatForever();
        Date start = new Date(System.currentTimeMillis() + initialDelay.toMillis());
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trg-" + name, "hitorro")
                .withSchedule(sb)
                .startAt(start)
                .build();
        submit(job, trigger);
        return job.getKey();
    }

    /** Cancel a previously-scheduled job. */
    public boolean cancel(JobKey key) {
        try {
            JobDetail detail = scheduler.getJobDetail(key);
            if (detail != null) {
                Object bodyId = detail.getJobDataMap().get(BODY_KEY);
                if (bodyId instanceof String s) bodies.remove(s);
            }
            return scheduler.deleteJob(key);
        } catch (SchedulerException e) {
            throw new RuntimeException("failed to cancel " + key, e);
        }
    }

    private JobDetail buildJob(String name, String bodyId) {
        JobDataMap data = new JobDataMap();
        data.put(BODY_KEY, bodyId);
        return JobBuilder.newJob(RunnableJob.class)
                .withIdentity(name, "hitorro")
                .usingJobData(data)
                .build();
    }

    private void submit(JobDetail job, Trigger trigger) {
        try {
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new RuntimeException("failed to schedule " + job.getKey(), e);
        }
    }

    private String registerBody(Runnable body) {
        String id = UUID.randomUUID().toString();
        bodies.put(id, body);
        return id;
    }

    /**
     * JobFactory that injects the owning scheduler's per-instance body registry into each
     * {@link RunnableJob} it constructs, so instances do not share state.
     */
    private static final class BodyResolvingJobFactory implements JobFactory {
        private final ConcurrentMap<String, Runnable> bodies;

        BodyResolvingJobFactory(ConcurrentMap<String, Runnable> bodies) {
            this.bodies = bodies;
        }

        @Override
        public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) {
            Class<? extends Job> jobClass = bundle.getJobDetail().getJobClass();
            if (jobClass == RunnableJob.class) {
                return new RunnableJob(bodies);
            }
            try {
                return jobClass.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("failed to instantiate " + jobClass, e);
            }
        }
    }

    /** Quartz job that looks up its body from the owning scheduler's registry. Public so Quartz can reference it. */
    public static final class RunnableJob implements Job {
        private final ConcurrentMap<String, Runnable> bodies;

        /** No-arg constructor retained for Quartz's default instantiation path (unused when a JobFactory is installed). */
        public RunnableJob() {
            this.bodies = null;
        }

        RunnableJob(ConcurrentMap<String, Runnable> bodies) {
            this.bodies = bodies;
        }

        @Override
        public void execute(JobExecutionContext ctx) throws JobExecutionException {
            if (bodies == null) {
                throw new JobExecutionException("RunnableJob constructed without a body registry; JobFactory not installed");
            }
            String bodyId = ctx.getMergedJobDataMap().getString(BODY_KEY);
            Runnable body = bodies.get(bodyId);
            if (body == null) return; // job was cancelled between fire selection and execution
            try {
                body.run();
            } catch (RuntimeException e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
