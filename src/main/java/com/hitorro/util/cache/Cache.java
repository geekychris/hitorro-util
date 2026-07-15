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

import java.time.Duration;
import java.util.function.Function;

/**
 * Minimal cache abstraction. Implementations may be in-process (see {@link InMemoryCache}),
 * distributed (see {@link RedisCache}), or a no-op (see {@link NoOpCache}) for tests.
 *
 * <p>Values equal to {@code null} are not cached; {@link #get} returns {@code null}
 * to mean "absent" whether the key was never inserted or has expired.
 */
public interface Cache<K, V> {

    /** Returns the value for the key, or {@code null} if absent or expired. */
    V get(K key);

    /** Insert with no expiry. */
    void put(K key, V value);

    /**
     * Insert with a TTL. Backends without native TTL emulate it lazily on {@link #get}.
     * A zero or negative TTL is equivalent to {@link #invalidate}.
     */
    void put(K key, V value, Duration ttl);

    /**
     * Returns cached value if present, otherwise computes, stores and returns it.
     *
     * <p><b>Not atomic.</b> The get/load/put sequence is composed of three separate operations —
     * concurrent callers racing on the same key may all miss, invoke {@code loader}, and race on
     * the final {@code put}. That is acceptable for idempotent loaders where duplicate work is
     * wasteful but harmless. For strictly single-flight behaviour, guard the call site yourself
     * (e.g. per-key mutex) or use a cache implementation that offers atomic compute-if-absent.
     */
    default V computeIfAbsent(K key, Function<K, V> loader) {
        V hit = get(key);
        if (hit != null) return hit;
        V loaded = loader.apply(key);
        if (loaded != null) put(key, loaded);
        return loaded;
    }

    /** Remove a single entry. */
    void invalidate(K key);

    /** Remove every entry. */
    void invalidateAll();

    /** Number of entries currently retained (best-effort — may include lazily-expired entries). */
    long size();
}
