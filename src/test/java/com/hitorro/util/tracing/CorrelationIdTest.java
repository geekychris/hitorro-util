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

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CorrelationId")
class CorrelationIdTest {

    @AfterEach
    void tearDown() {
        CorrelationId.clear();
    }

    @Test
    @DisplayName("set/get round-trip via MDC")
    void setGet() {
        CorrelationId.set("abc-123");
        assertThat(CorrelationId.get()).isEqualTo("abc-123");
        assertThat(MDC.get(CorrelationId.MDC_KEY)).isEqualTo("abc-123");
    }

    @Test
    @DisplayName("clear removes the entry")
    void clearRemoves() {
        CorrelationId.set("abc");
        CorrelationId.clear();
        assertThat(CorrelationId.get()).isNull();
    }

    @Test
    @DisplayName("set(null) clears the entry")
    void setNullClears() {
        CorrelationId.set("abc");
        CorrelationId.set(null);
        assertThat(CorrelationId.get()).isNull();
    }

    @Test
    @DisplayName("generate returns and sets a non-empty id")
    void generateReturns() {
        String id = CorrelationId.generate();
        assertThat(id).isNotBlank();
        assertThat(CorrelationId.get()).isEqualTo(id);
    }

    @Test
    @DisplayName("with(...) restores prior value on exit — even after exception")
    void withRestores() {
        CorrelationId.set("outer");
        AtomicReference<String> inner = new AtomicReference<>();
        Runnable body = () -> {
            inner.set(CorrelationId.get());
            throw new RuntimeException("boom");
        };
        try {
            CorrelationId.with("inner", body);
        } catch (RuntimeException expected) {
            // swallow
        }
        assertThat(inner.get()).isEqualTo("inner");
        assertThat(CorrelationId.get()).isEqualTo("outer");
    }

    @Test
    @DisplayName("with(Callable) returns the callable's value")
    void withCallable() throws Exception {
        String result = CorrelationId.with("x", () -> "answer");
        assertThat(result).isEqualTo("answer");
        assertThat(CorrelationId.get()).isNull();
    }

    @Test
    @DisplayName("set rejects IDs containing control characters (e.g. newline)")
    void setRejectsControlCharacters() {
        assertThatThrownBy(() -> CorrelationId.set("abc\ndef"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("control character");
        assertThat(CorrelationId.get()).isNull();
    }

    @Test
    @DisplayName("set rejects IDs longer than 128 characters")
    void setRejectsOverlongId() {
        assertThatThrownBy(() -> CorrelationId.set("a".repeat(200)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max length");
        assertThat(CorrelationId.get()).isNull();
    }

    @Test
    @DisplayName("set rejects empty string")
    void setRejectsEmpty() {
        assertThatThrownBy(() -> CorrelationId.set(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be empty");
        assertThat(CorrelationId.get()).isNull();
    }
}
