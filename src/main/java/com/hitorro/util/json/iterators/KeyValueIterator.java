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
package com.hitorro.util.json.iterators;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.JSONElement;

/**
 * Reads a stream of JSON objects of which one is a key and the other is a value.  We pair these up and put them in a
 * GKV.
 */
public class KeyValueIterator<K, V> extends AbstractIterator<GenericKeyValue<K, V>> {
    private AbstractIterator iter;

    public KeyValueIterator(AbstractIterator<JSONElement> iter) {
        this.iter = iter;
    }

    @Override
    public void close() throws Exception {
        iter.close();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public GenericKeyValue<K, V> next() {
        K key = (K) iter.next();
        if (iter.hasNext()) {
            V value = (V) iter.next();
            return get(key, value);
        }
        return null;
    }

    protected GenericKeyValue<K, V> get(K key, V value) {
        return new GenericKeyValue<K, V>(key, value);
    }

    @Override
    public void remove() {
    }
}
