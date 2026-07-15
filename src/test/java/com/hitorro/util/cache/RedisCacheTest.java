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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link RedisCache}. Only pure/package-visible helpers and the constructor
 * contract are covered here — exercising get/put/SCAN behavior would require a mock Lettuce
 * client (Mockito is not a dependency of this module) or an embedded Redis, both out of
 * scope for a util-module test.
 */
@DisplayName("RedisCache")
class RedisCacheTest {

    @Test
    @DisplayName("escapeGlob escapes Redis SCAN MATCH metacharacters")
    void escapeGlobMetachars() {
        assertThat(RedisCache.escapeGlob("plain")).isEqualTo("plain");
        assertThat(RedisCache.escapeGlob("a*b")).isEqualTo("a\\*b");
        assertThat(RedisCache.escapeGlob("a?b")).isEqualTo("a\\?b");
        assertThat(RedisCache.escapeGlob("a[bc]d")).isEqualTo("a\\[bc\\]d");
        // Backslash is escaped first so we don't double-escape our own escapes
        assertThat(RedisCache.escapeGlob("a\\b")).isEqualTo("a\\\\b");
        // Real-world prefix a caller might use
        assertThat(RedisCache.escapeGlob("cache:user[42]:*:"))
                .isEqualTo("cache:user\\[42\\]:\\*:");
    }

    @Test
    @DisplayName("escapeGlob is a no-op for prefixes with no metacharacters")
    void escapeGlobNoop() {
        assertThat(RedisCache.escapeGlob("cache:v1:user:")).isEqualTo("cache:v1:user:");
    }

    @Test
    @DisplayName("escapeGlob handles empty string")
    void escapeGlobEmpty() {
        assertThat(RedisCache.escapeGlob("")).isEmpty();
    }

    @Test
    @DisplayName("constructor rejects null keyPrefix")
    void constructorRejectsNullPrefix() {
        assertThatThrownBy(() -> new RedisCache<String, String>(null, null,
                Object::toString, Object::toString, s -> s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyPrefix");
    }

    @Test
    @DisplayName("constructor rejects empty keyPrefix")
    void constructorRejectsEmptyPrefix() {
        assertThatThrownBy(() -> new RedisCache<String, String>(null, "",
                Object::toString, Object::toString, s -> s))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyPrefix");
    }
}
