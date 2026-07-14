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
package com.hitorro.util.osprocessexec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Fluent process runner with timeout, kill-tree, stdin, and per-line streaming callbacks.
 *
 * <pre>{@code
 * ProcessResult r = ProcessRunner.of("sh", "-c", "echo hi && sleep 0.1")
 *     .timeout(Duration.ofSeconds(2))
 *     .onStdout(System.out::println)
 *     .run();
 * if (r.exitCode() != 0) throw new IllegalStateException("bad exit");
 * }</pre>
 *
 * <p>{@link #killTree(boolean)} enables recursive descendant kill on timeout — safer for shells that
 * spawn children. Off by default because it changes semantics for callers who wrap wrappers.
 */
public final class ProcessRunner {

    private final List<String> command = new ArrayList<>();
    private File workingDir;
    private Duration timeout;
    private boolean killTree = false;
    private byte[] stdin;
    private Consumer<String> stdoutLine;
    private Consumer<String> stderrLine;
    private boolean captureStdout = true;
    private boolean captureStderr = true;

    private ProcessRunner(List<String> command) {
        this.command.addAll(command);
    }

    public static ProcessRunner of(String... command) {
        return new ProcessRunner(Arrays.asList(command));
    }

    public static ProcessRunner of(List<String> command) {
        return new ProcessRunner(command);
    }

    public ProcessRunner workingDir(File dir) { this.workingDir = dir; return this; }
    public ProcessRunner timeout(Duration d)  { this.timeout = d; return this; }
    public ProcessRunner killTree(boolean on) { this.killTree = on; return this; }
    public ProcessRunner stdin(String s)      { this.stdin = s == null ? null : s.getBytes(StandardCharsets.UTF_8); return this; }
    public ProcessRunner stdin(byte[] b)      { this.stdin = b; return this; }

    /** Callback invoked with each stdout line (LF/CRLF stripped). */
    public ProcessRunner onStdout(Consumer<String> cb) { this.stdoutLine = cb; return this; }
    public ProcessRunner onStderr(Consumer<String> cb) { this.stderrLine = cb; return this; }

    /** Skip capturing stdout into the result (still routed through {@link #onStdout} if set). */
    public ProcessRunner discardStdout() { this.captureStdout = false; return this; }
    public ProcessRunner discardStderr() { this.captureStderr = false; return this; }

    public ProcessResult run() {
        ProcessBuilder pb = new ProcessBuilder(command);
        if (workingDir != null) pb.directory(workingDir);
        Process proc;
        try {
            proc = pb.start();
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start " + command, e);
        }

        // Two virtual pump threads keep the process from blocking on full pipe buffers.
        ExecutorService pumps = Executors.newThreadPerTaskExecutor(r -> {
            Thread t = Thread.ofVirtual().unstarted(r);
            t.setName("proc-pump");
            return t;
        });
        Future<StringBuilder> outFuture = pumps.submit(() -> pump(proc.getInputStream(), stdoutLine, captureStdout));
        Future<StringBuilder> errFuture = pumps.submit(() -> pump(proc.getErrorStream(), stderrLine, captureStderr));

        if (stdin != null) {
            try (OutputStream os = proc.getOutputStream()) {
                os.write(stdin);
            } catch (IOException e) {
                pumps.shutdownNow();
                throw new UncheckedIOException("failed to write stdin", e);
            }
        } else {
            try { proc.getOutputStream().close(); } catch (IOException ignored) {}
        }

        boolean timedOut = false;
        int exitCode;
        try {
            if (timeout == null) {
                exitCode = proc.waitFor();
            } else {
                boolean finished = proc.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
                if (!finished) {
                    timedOut = true;
                    killProcess(proc);
                }
                exitCode = proc.exitValue();
            }
        } catch (InterruptedException e) {
            killProcess(proc);
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while waiting for " + command, e);
        } catch (IllegalThreadStateException e) {
            // Race: process exited between kill and exitValue().
            exitCode = -1;
        }

        StringBuilder out, err;
        try {
            out = outFuture.get(1, TimeUnit.SECONDS);
            err = errFuture.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            out = new StringBuilder();
            err = new StringBuilder();
        } finally {
            pumps.shutdownNow();
        }

        return new ProcessResult(exitCode, out.toString(), err.toString(), timedOut);
    }

    private void killProcess(Process proc) {
        if (killTree) {
            proc.descendants().forEach(ProcessHandle::destroyForcibly);
        }
        proc.destroyForcibly();
    }

    private static StringBuilder pump(InputStream in, Consumer<String> lineCb, boolean capture) {
        StringBuilder buf = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (capture) {
                    buf.append(line).append('\n');
                }
                if (lineCb != null) {
                    try { lineCb.accept(line); } catch (RuntimeException ignored) {}
                }
            }
        } catch (IOException ignored) {
            // Stream closed on process exit — expected.
        }
        return buf;
    }

    public record ProcessResult(int exitCode, String stdout, String stderr, boolean timedOut) {
        public boolean succeeded() { return exitCode == 0 && !timedOut; }
    }
}
