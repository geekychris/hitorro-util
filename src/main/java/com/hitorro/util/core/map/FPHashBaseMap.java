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
package com.hitorro.util.core.map;


import gnu.trove.map.hash.TLongLongHashMap;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.longword.opers.LongOperator;

import java.util.List;

/**
 *
 */
public class FPHashBaseMap implements FPHashBaseMapInterface {
    public static final int defaultInitialSize = 1000000;
    protected TLongLongHashMap map;
    protected WordBits bits;
    protected BaseMapper<String, Long> keyMapping;
    protected int layer;

    public void init(int layer, WordBits bits, int initialSize, BaseMapper<String, Long> keyMapping, boolean lowerCase) {
        this.layer = layer;
        map = new TLongLongHashMap(initialSize);
        this.bits = bits;
        this.keyMapping = keyMapping;
    }

    public int getLayer() {
        return layer;
    }

    public FPHashBaseMap setPhrases(List<GenericKeyValue<String, Integer>> tokens, String wordBitName) {
        return this;
    }

    public TLongLongHashMap getMap(int layer) {
        return map;
    }

    public int contains(long key) {
        if (map.contains(key)) {
            return layer;
        }
        return -1;
    }

    public int match(String key, LongOperator oper, int layer) {
        if (oper.match(get(key, layer))) {
            return layer;
        }
        return -1;
    }

    public int match(long fp, LongOperator oper, int layer) {
        if (oper.match(get(fp, layer))) {
            return layer;
        }
        return -1;
    }

    public long get(String key, int layer) {
        long fp = getHash(key);
        return map.get(fp);
    }

    public long get(long fp, int layer) {
        return map.get(fp);
    }

    public WordBits getWordBits(int layer) {
        return bits;
    }

    protected long getHash(String v) {
        return keyMapping.apply(v);
    }

    /**
     * @param m - apply for the wordbit name to value mapping
     * @param v - token to update
     */
    public void updateRecord(final JVS m, final String v, int layer) {
        long fp = getHash(v);
        long value = map.get(fp);
        value = bits.setFromMap(m, value);
        map.put(fp, value);
    }
}
