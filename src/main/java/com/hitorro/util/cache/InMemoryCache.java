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
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Thread-safe in-process cache with optional per-entry TTL. Expiry is enforced lazily
 * on {@link #get}; no background eviction thread. No size cap — for LRU/size-bounded
 * behaviour prefer Caffeine (not a dependency of this module).
 */
public class InMemoryCache<K, V> implements Cache<K, V> {

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final LongSupplier clock;

    public InMemoryCache() {
        this(System::nanoTime);
    }

    /** Package-private ctor for time-controllable tests. */
    InMemoryCache(LongSupplier nanoClock) {
        this.clock = nanoClock;
    }

    @Override
    public V get(K key) {
        Entry<V> e = map.get(key);
        if (e == null) return null;
        if (e.isExpired(clock.getAsLong())) {
            map.remove(key, e);
            return null;
        }
        return e.value;
    }

    @Override
    public void put(K key, V value) {
        if (value == null) return;
        map.put(key, new Entry<>(value, 0L));
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        if (value == null) return;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            map.remove(key);
            return;
        }
        long expiresAt = clock.getAsLong() + ttl.toNanos();
        map.put(key, new Entry<>(value, expiresAt));
    }

    @Override
    public void invalidate(K key) {
        map.remove(key);
    }

    @Override
    public void invalidateAll() {
        map.clear();
    }

    @Override
    public long size() {
        return map.size();
    }

    private static final class Entry<V> {
        final V value;
        final long expiresAtNanos; // 0 = no expiry

        Entry(V value, long expiresAtNanos) {
            this.value = value;
            this.expiresAtNanos = expiresAtNanos;
        }

        boolean isExpired(long nowNanos) {
            return expiresAtNanos != 0L && nowNanos >= expiresAtNanos;
        }
    }
}
