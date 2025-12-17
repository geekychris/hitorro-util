/*
 * Copyright (c) 2006-2025 Chris Collins
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
package com.hitorro.util;

import com.hitorro.util.core.map.MapFactory;

import java.util.Map;
import java.util.Set;

/**
 * Maintains counts of (key, value) pairs.  The apply is structured so
 * that for every key, one can get a counter over values.  Example
 * usage: keys might be words with values being POS tags, and the
 * count being the number of occurences of that word/tag pair.  The
 * sub-counters returned by getCounter(word) would be count
 * distributions over tags for that word.
 *
 * @author Dan Klein
 */
public class CounterMap<K, V> {

    private MapFactory<V, Double> mf;
    private Map<K, Counter<V>> counterMap;

    // -----------------------------------------------------------------------

    public CounterMap() {
        this(new MapFactory.HashMapFactory<K, Counter<V>>(),
                new MapFactory.HashMapFactory<V, Double>());
    }

    public CounterMap(MapFactory<K, Counter<V>> outerMF,
                      MapFactory<V, Double> innerMF) {
        mf = innerMF;
        counterMap = outerMF.buildMap();
    }

    public static void main(String[] args) {
        CounterMap<String, String> bigramCounterMap = new CounterMap<String, String>();
        bigramCounterMap.incrementCount("people", "run", 1);
        bigramCounterMap.incrementCount("cats", "growl", 2);
        bigramCounterMap.incrementCount("cats", "scamper", 3);
        System.out.println(bigramCounterMap);
        System.out.println("Entries for cats: " + bigramCounterMap.getCounter("cats"));
        System.out.println("Entries for dogs: " + bigramCounterMap.getCounter("dogs"));
        System.out.println("Count of cats scamper: " + bigramCounterMap.getCount("cats", "scamper"));
        System.out.println("Count of snakes slither: " + bigramCounterMap.getCount("snakes", "slither"));
        System.out.println("Total size: " + bigramCounterMap.totalSize());
        System.out.println("Total count: " + bigramCounterMap.totalCount());
        System.out.println(bigramCounterMap);
    }

    // -----------------------------------------------------------------------

    public void clear() {
        counterMap.clear();
    }

    protected Counter<V> ensureCounter(K key) {
        Counter<V> valueCounter = counterMap.get(key);
        if (valueCounter == null) {
            valueCounter = new Counter<V>(mf);
            counterMap.put(key, valueCounter);
        }
        return valueCounter;
    }

    /**
     * Returns the keys that have been inserted into this CounterMap.
     */
    public Set<K> keySet() {
        return counterMap.keySet();
    }

    /**
     * Sets the count for a particular (key, value) pair.
     */
    public void setCount(K key, V value, double count) {
        Counter<V> valueCounter = ensureCounter(key);
        valueCounter.setCount(value, count);
    }

    /**
     * Increments the count for a particular (key, value) pair.
     */
    public void incrementCount(K key, V value, double count) {
        Counter<V> valueCounter = ensureCounter(key);
        valueCounter.incrementCount(value, count);
    }

    /**
     * Gets the count of the given (key, value) entry, or zero if that
     * entry is not present.  Does not create any objects.
     */
    public double getCount(K key, V value) {
        Counter<V> valueCounter = counterMap.get(key);
        if (valueCounter == null) {
            return 0.0;
        }
        return valueCounter.getCount(value);
    }

    /**
     * Gets the sub-counter for the given key.  If there is none, a
     * counter is created for that key, and installed in the CounterMap.
     * You can, for example, put to the returned empty counter directly
     * (though you shouldn't).  This is so whether the key is present or
     * not, modifying the returned counter has the same effect (but
     * don't do it).
     */
    public Counter<V> getCounter(K key) {
        return ensureCounter(key);
    }

    /**
     * Returns the total of all counts in sub-counters.  This
     * implementation is linear; it recalculates the total each time.
     */
    public double totalCount() {
        double total = 0.0;
        for (Map.Entry<K, Counter<V>> entry : counterMap.entrySet()) {
            Counter<V> counter = entry.getValue();
            total += counter.totalCount();
        }
        return total;
    }

    /**
     * Returns the total number of (key, value) entries in the
     * CounterMap (not their total counts).
     */
    public int totalSize() {
        int total = 0;
        for (Map.Entry<K, Counter<V>> entry : counterMap.entrySet()) {
            Counter<V> counter = entry.getValue();
            total += counter.size();
        }
        return total;
    }

    /**
     * The number of keys in this CounterMap (not the number of
     * key-value entries -- use totalSize() for that)
     */
    public int size() {
        return counterMap.size();
    }

    /**
     * True if there are no entries in the CounterMap (false does not
     * mean totalCount > 0)
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    // -----------------------------------------------------------------------

    public String toString() {
        StringBuilder sb = new StringBuilder("[\n");
        for (Map.Entry<K, Counter<V>> entry : counterMap.entrySet()) {
            sb.append("  ");
            sb.append(entry.getKey());
            sb.append(" -> ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
