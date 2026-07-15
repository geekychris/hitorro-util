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

import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

/**
 * Cache backed by a Lettuce sync connection. Keys and values are serialised via caller-supplied
 * functions; TTL uses native Redis expiry ({@code SET ... PX}).
 *
 * <p>{@link #invalidateAll()} deletes only the entries whose keys match {@link #keyPrefix} using
 * cursor-based {@code SCAN} iteration — it does <b>not</b> issue {@code FLUSHDB} and never blocks
 * Redis with {@code KEYS}. The constructor rejects a null or empty {@code keyPrefix} so callers
 * cannot accidentally produce a global {@code "*"} scan pattern that would touch unrelated keys;
 * each cache sharing a Redis instance must therefore be constructed with a distinct prefix.
 *
 * <p>{@link #size()} performs the same cursor-based {@code SCAN} with the prefix; treat it as an
 * estimate on large datasets and avoid calling it on a hot path.
 */
public class RedisCache<K, V> implements Cache<K, V> {

    private final RedisCommands<String, String> redis;
    private final String keyPrefix;
    private final Function<K, String> keySer;
    private final Function<V, String> valueSer;
    private final Function<String, V> valueDeser;

    public RedisCache(RedisCommands<String, String> redis,
                      String keyPrefix,
                      Function<K, String> keySer,
                      Function<V, String> valueSer,
                      Function<String, V> valueDeser) {
        if (keyPrefix == null || keyPrefix.isEmpty()) {
            throw new IllegalArgumentException("keyPrefix must be non-empty");
        }
        this.redis = redis;
        this.keyPrefix = keyPrefix;
        this.keySer = keySer;
        this.valueSer = valueSer;
        this.valueDeser = valueDeser;
    }

    private String fullKey(K key) {
        return keyPrefix + keySer.apply(key);
    }

    @Override
    public V get(K key) {
        String raw = redis.get(fullKey(key));
        return raw == null ? null : valueDeser.apply(raw);
    }

    @Override
    public void put(K key, V value) {
        if (value == null) return;
        redis.set(fullKey(key), valueSer.apply(value));
    }

    @Override
    public void put(K key, V value, Duration ttl) {
        if (value == null) return;
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            invalidate(key);
            return;
        }
        redis.psetex(fullKey(key), ttl.toMillis(), valueSer.apply(value));
    }

    @Override
    public void invalidate(K key) {
        redis.del(fullKey(key));
    }

    @Override
    public void invalidateAll() {
        ScanArgs args = ScanArgs.Builder.matches(keyPrefix + "*");
        ScanCursor cursor = ScanCursor.INITIAL;
        do {
            KeyScanCursor<String> result = redis.scan(cursor, args);
            List<String> keys = result.getKeys();
            if (keys != null && !keys.isEmpty()) {
                redis.del(keys.toArray(new String[0]));
            }
            cursor = result;
        } while (!cursor.isFinished());
    }

    @Override
    public long size() {
        ScanArgs args = ScanArgs.Builder.matches(keyPrefix + "*");
        ScanCursor cursor = ScanCursor.INITIAL;
        long count = 0L;
        do {
            KeyScanCursor<String> result = redis.scan(cursor, args);
            List<String> keys = result.getKeys();
            if (keys != null) {
                count += keys.size();
            }
            cursor = result;
        } while (!cursor.isFinished());
        return count;
    }
}
