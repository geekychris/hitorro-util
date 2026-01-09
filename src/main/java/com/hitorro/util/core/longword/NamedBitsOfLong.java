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
package com.hitorro.util.core.longword;

import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.Fmt;

/**
 * Manipulate a bit of a long
 */
public class NamedBitsOfLong {
    public static final long AllOn = 0xFFFFFFFFFFFFFFFFl;
    private int bitOffset;
    private long m_bits;
    private int m_width = 1;
    private long m_mask;
    private String m_name;
    private BiDirectionalKeyLongMap valueMapper;
    private WordBits wordBits;

    public NamedBitsOfLong() {

    }

    public NamedBitsOfLong(WordBits wordBits, String name, int position, int width, BiDirectionalKeyLongMap valueMapper) {
        setBitOffset(name, position, width, wordBits);
        this.valueMapper = valueMapper;
    }

    public NamedBitsOfLong(WordBits wordBits, String name, int position, int width) {
        setBitOffset(name, position, width, wordBits);
    }

    public String mapFromLong(long l) {
        return valueMapper.mapToString(get(l));
    }

    public long getLongHash() {
        String v = Fmt.S("%s.%s.%s", m_name, bitOffset, m_width);
        return FPHash64.getFP(v);
    }

    public int getWidth() {
        return m_width;
    }

    public void setBitOffset(String name, int position, int width, WordBits wordBits) {
        m_name = name.toLowerCase();
        bitOffset = position;
        m_width = width;
        this.wordBits = wordBits;
        if (m_width == 1) {
            m_bits = 1 << position;

        } else {
            long bits = 0;
            long bit = 0;
            for (int i = 0; i < m_width; i++) {
                int pos = position + i;
                bit = 1l << pos;
                bits = bits | bit;
            }
            m_bits = bits;
        }
        m_mask = AllOn ^ m_bits;
    }

    public String getName() {
        return m_name;
    }

    public int getBitOffset() {
        return bitOffset;
    }

    public long set(long val) {
        return val | m_bits;
    }

    public long set(long word, String number) {
        return set(word, valueMapper.mapFromString(number));
    }

    public boolean hasValueMapper() {
        return valueMapper != null;
    }

    public long set(long word, long number) {
        return word = (word & m_mask) | (number << bitOffset) & m_bits;
    }

    public long get(long word) {
        return (word & m_bits) >> bitOffset;
    }

    public long clear(long val) {
        return val & m_mask;
    }

    public boolean isSet(long val) {
        return (val & m_bits) != 0;
    }

    public void dumpKeyValueToStringBuilder(StringBuilder b, long l) {
        b.append(m_name);
        b.append("=");
        if (m_width == 1) {
            if (isSet(l)) {
                b.append("1");
            } else {
                b.append("0");
            }
        } else {
            long v = get(l);
            b.append(v);
        }
    }
}
