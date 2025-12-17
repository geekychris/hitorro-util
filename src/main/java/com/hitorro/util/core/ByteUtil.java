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
package com.hitorro.util.core;

/**
 *
 */
public class ByteUtil {
    public static final int Mask = 0xff;

    /**
     * Given i, create a set of 1's of i bits  Used for masking variable high low parts of a long
     *
     * @param i
     * @return
     */
    public static final long getLowPartMaskNBits(int i) {
        long ret = 0;
        long one = 1;
        for (long j = 0; j < i; j++) {
            ret = ret | one << j;
        }
        return ret;
    }

    public static final long getHighPartMaskNBits(int i) {

        long ret = 0;
        long one = 1;
        for (long j = i; j < 64; j++) {
            ret = ret | one << j;
        }
        return ret;
    }

    public static final long combineIntByte(int i, byte b) {
        long l = i;
        i = i << 8;
        return i | b;
    }

    public static final int intByteIntPart(long l) {
        return (int) (l >> 8);
    }

    public static final byte intByteBytePart(long l) {
        return (byte) l;
    }

    /**
     * get an int value out of the high part of a short with max value of 0-127
     *
     * @param s
     * @return
     */
    public static final int getHighPartOfShortAsInt(short s) {
        return ((s & 0xff00) >> 8) - 127;
    }

    /**
     * get an int value out of the high part of a short with max value of 0-127
     *
     * @param s
     * @return
     */
    public static final int getLowOfShortAsInt(short s) {
        return (s & 0xff) - 127;
    }

    public static short combineTwoIntsInShort(int a, int b) {
        int ab = getShortFromByte(a);

        ab = ab << 8;
        int bb = getShortFromByte(b) & Mask;
        return (short) (ab | bb);
    }

    private static final int getShortFromByte(int a) {
        if (a >= -127 && a <= 128) {
            a += 127;
        }
        return (short) a;
    }
}


