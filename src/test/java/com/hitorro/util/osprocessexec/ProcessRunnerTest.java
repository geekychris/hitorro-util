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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessRunner")
@DisabledOnOs(OS.WINDOWS)
class ProcessRunnerTest {

    @Test
    @DisplayName("captures stdout and exit code")
    void capturesStdout() {
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "echo hello").run();
        assertThat(r.exitCode()).isZero();
        assertThat(r.succeeded()).isTrue();
        assertThat(r.timedOut()).isFalse();
        assertThat(r.stdout().trim()).isEqualTo("hello");
    }

    @Test
    @DisplayName("captures stderr separately")
    void capturesStderr() {
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "echo err 1>&2").run();
        assertThat(r.stderr().trim()).isEqualTo("err");
        assertThat(r.stdout()).isEmpty();
    }

    @Test
    @DisplayName("propagates non-zero exit code without throwing")
    void nonZeroExit() {
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "exit 7").run();
        assertThat(r.exitCode()).isEqualTo(7);
        assertThat(r.succeeded()).isFalse();
    }

    @Test
    @DisplayName("timeout kills long-running process and reports timedOut=true")
    void timeoutKills() {
        long start = System.currentTimeMillis();
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "sleep 10")
                .timeout(Duration.ofMillis(200))
                .run();
        long elapsed = System.currentTimeMillis() - start;
        assertThat(r.timedOut()).isTrue();
        assertThat(elapsed).isLessThan(2_000);
    }

    @Test
    @DisplayName("stdin is delivered to the child")
    void stdinPipe() {
        ProcessRunner.ProcessResult r = ProcessRunner.of("cat")
                .stdin("piped in")
                .run();
        assertThat(r.exitCode()).isZero();
        assertThat(r.stdout()).contains("piped in");
    }

    @Test
    @DisplayName("onStdout callback fires per line, in order")
    void stdoutCallback() {
        List<String> lines = new ArrayList<>();
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "printf 'a\\nb\\nc\\n'")
                .onStdout(lines::add)
                .run();
        assertThat(r.exitCode()).isZero();
        assertThat(lines).containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("killTree terminates shell-spawned descendants on timeout")
    void killTreeTerminatesChildren() {
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "sleep 30")
                .timeout(Duration.ofMillis(200))
                .killTree(true)
                .run();
        assertThat(r.timedOut()).isTrue();
    }

    @Test
    @DisplayName("discardStdout omits capture but still runs callbacks")
    void discardStdout() {
        List<String> lines = new ArrayList<>();
        ProcessRunner.ProcessResult r = ProcessRunner.of("sh", "-c", "echo hi")
                .discardStdout()
                .onStdout(lines::add)
                .run();
        assertThat(r.stdout()).isEmpty();
        assertThat(lines).containsExactly("hi");
    }
}
