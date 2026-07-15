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
package com.hitorro.util.tracing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MdcContext")
class MdcContextTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("wrap(Runnable) installs the captured MDC in the executing thread")
    void wrapRunnableInstallsSnapshot() throws Exception {
        MDC.put("corrId", "abc-123");
        MdcContext ctx = MdcContext.capture();
        MDC.clear(); // simulate a different (empty) thread

        AtomicReference<String> observed = new AtomicReference<>();
        Runnable body = () -> observed.set(MDC.get("corrId"));

        Thread t = new Thread(ctx.wrap(body));
        t.start();
        t.join(2_000);

        assertThat(observed.get()).isEqualTo("abc-123");
    }

    @Test
    @DisplayName("wrap(Callable) returns the value AND installs the captured MDC")
    void wrapCallableReturnsValue() throws Exception {
        MDC.put("corrId", "xyz");
        MdcContext ctx = MdcContext.capture();

        Callable<String> body = () -> "id=" + MDC.get("corrId");
        String result = ctx.wrap(body).call();

        assertThat(result).isEqualTo("id=xyz");
    }

    @Test
    @DisplayName("wrap restores the executing thread's prior MDC on exit")
    void restoresPriorMdc() throws Exception {
        MDC.put("corrId", "outer");
        MdcContext ctx = MdcContext.capture();

        // The executing "worker" already has its own MDC state.
        MDC.clear();
        MDC.put("worker", "w1");

        ctx.wrap(() -> {
            assertThat(MDC.get("corrId")).isEqualTo("outer");
            assertThat(MDC.get("worker")).isNull(); // snapshot replaces
        }).run();

        // Prior worker state fully restored — captured snapshot is gone.
        assertThat(MDC.get("worker")).isEqualTo("w1");
        assertThat(MDC.get("corrId")).isNull();
    }

    @Test
    @DisplayName("wrap restores prior MDC even when the body throws")
    void restoresOnException() {
        MDC.put("corrId", "outer");
        MdcContext ctx = MdcContext.capture();

        MDC.clear();
        MDC.put("worker", "w1");

        Runnable throwing = () -> { throw new RuntimeException("boom"); };
        assertThatThrownBy(() -> ctx.wrap(throwing).run())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        assertThat(MDC.get("worker")).isEqualTo("w1");
        assertThat(MDC.get("corrId")).isNull();
    }

    @Test
    @DisplayName("capture of an empty MDC works")
    void emptyCapture() {
        MDC.clear();
        MdcContext ctx = MdcContext.capture();

        MDC.put("worker", "w1");
        ctx.wrap(() -> assertThat(MDC.get("worker")).isNull()).run();
        assertThat(MDC.get("worker")).isEqualTo("w1");
    }

    @Test
    @DisplayName("snapshot is a value at capture time — later MDC changes don't leak in")
    void snapshotIsImmutableAtCaptureTime() throws Exception {
        MDC.put("corrId", "at-capture");
        MdcContext ctx = MdcContext.capture();
        MDC.put("corrId", "after-capture");

        Callable<String> body = () -> MDC.get("corrId");
        assertThat(ctx.wrap(body).call()).isEqualTo("at-capture");
    }
}
