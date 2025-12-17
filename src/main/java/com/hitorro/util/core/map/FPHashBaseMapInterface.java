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
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.longword.opers.LongOperator;

/**
 *
 */
public interface FPHashBaseMapInterface {
    TLongLongHashMap getMap(int layer);

    int contains(long key);

    int match(String key, LongOperator oper, int layer);

    int match(long fp, LongOperator oper, int layer);

    long get(String key, int layer);

    long get(long fp, int layer);

    WordBits getWordBits(int layer);


    /**
     * @param m - apply for the wordbit name to value mapping
     * @param v - token to update
     */
    void updateRecord(final JVS m, final String v, int layer);
}
