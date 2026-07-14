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
package com.hitorro.util.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("InMemoryCache")
class InMemoryCacheTest {

    @Test
    @DisplayName("put/get round-trips values")
    void putGet() {
        Cache<String, Integer> c = new InMemoryCache<>();
        c.put("a", 1);
        c.put("b", 2);
        assertThat(c.get("a")).isEqualTo(1);
        assertThat(c.get("b")).isEqualTo(2);
        assertThat(c.get("missing")).isNull();
        assertThat(c.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("null values are ignored")
    void nullValueIgnored() {
        Cache<String, String> c = new InMemoryCache<>();
        c.put("k", null);
        assertThat(c.get("k")).isNull();
        assertThat(c.size()).isZero();
    }

    @Test
    @DisplayName("TTL expiry is enforced on get")
    void ttlExpiry() {
        AtomicLong now = new AtomicLong(0L);
        InMemoryCache<String, String> c = new InMemoryCache<>(now::get);
        c.put("k", "v", Duration.ofNanos(100));
        assertThat(c.get("k")).isEqualTo("v");
        now.set(200L);
        assertThat(c.get("k")).isNull();
        assertThat(c.size()).isZero(); // lazily removed
    }

    @Test
    @DisplayName("zero or negative TTL is equivalent to invalidate")
    void nonPositiveTtlEvicts() {
        Cache<String, String> c = new InMemoryCache<>();
        c.put("k", "v");
        c.put("k", "v", Duration.ZERO);
        assertThat(c.get("k")).isNull();
    }

    @Test
    @DisplayName("invalidate removes single key")
    void invalidateOne() {
        Cache<String, Integer> c = new InMemoryCache<>();
        c.put("a", 1);
        c.put("b", 2);
        c.invalidate("a");
        assertThat(c.get("a")).isNull();
        assertThat(c.get("b")).isEqualTo(2);
    }

    @Test
    @DisplayName("invalidateAll clears everything")
    void invalidateAll() {
        Cache<String, Integer> c = new InMemoryCache<>();
        c.put("a", 1);
        c.put("b", 2);
        c.invalidateAll();
        assertThat(c.size()).isZero();
    }

    @Test
    @DisplayName("computeIfAbsent memoizes the loader")
    void computeIfAbsent() {
        Cache<String, Integer> c = new InMemoryCache<>();
        AtomicInteger calls = new AtomicInteger();
        Integer first = c.computeIfAbsent("k", k -> calls.incrementAndGet());
        Integer second = c.computeIfAbsent("k", k -> calls.incrementAndGet());
        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(1);
        assertThat(calls).hasValue(1);
    }

    @Test
    @DisplayName("NoOpCache never stores anything")
    void noOp() {
        Cache<String, Integer> c = new NoOpCache<>();
        c.put("a", 1);
        c.put("b", 2, Duration.ofSeconds(30));
        assertThat(c.get("a")).isNull();
        assertThat(c.size()).isZero();
        AtomicInteger calls = new AtomicInteger();
        Integer v = c.computeIfAbsent("x", k -> calls.incrementAndGet());
        assertThat(v).isEqualTo(1);
        Integer v2 = c.computeIfAbsent("x", k -> calls.incrementAndGet());
        assertThat(v2).isEqualTo(2); // recomputed every time
    }
}
