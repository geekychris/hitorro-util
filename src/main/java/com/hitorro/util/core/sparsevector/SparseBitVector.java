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
package com.hitorro.util.core.sparsevector;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 26, 2004 Time: 11:23:45 AM
 * <p/>
 * Description:
 * <p/>
 * This is not thread safe, to avoid stack variables most variables are scoped to the class
 */
public class SparseBitVector {
    private int[] m_array[];
    private int blockSizeToPower;
    private int m_blockSize;
    private int blockSizeToPowerPlusBitOffset;
    private int arraySize;
    private int bitsPerBlock;
    private int m_bits;
    private int wordMask;

    private int t_blockNumber;
    private int t_wordNumber;
    private int t_bitNumber;
    private int t_bit;
    private int t_inverseBit;

    public SparseBitVector(int blockSizeToPower, int initialArraySize) {
        arraySize = initialArraySize;
        this.blockSizeToPower = blockSizeToPower;
        blockSizeToPowerPlusBitOffset = this.blockSizeToPower + 5;
        m_blockSize = 1 << blockSizeToPower;
        m_bits = 32;
        bitsPerBlock = m_blockSize * m_bits; // using integers
        m_array = new int[arraySize][];
        wordMask = createWordMask(blockSizeToPower - 1);
    }

    public final boolean getBit(long address) {
        // rotate away the bitoffset
        computeBits(address);

        if (t_blockNumber >= arraySize) {
            // not even in block range
            return false;
        }

        if (m_array[t_blockNumber] == null) {
            return false;
        }

        // must be != rather than > as number
        return !((m_array[t_blockNumber][t_wordNumber] & t_bit) == 0);
    }

    public final void setBit(long address) {
        computeBits(address);
        if (t_blockNumber >= arraySize) {
            increaseArraySize();
        }

        if (m_array[t_blockNumber] == null) {
            m_array[t_blockNumber] = SparseBitVectorBlockPool.getBlock(blockSizeToPower);
        }
        m_array[t_blockNumber][t_wordNumber] |= t_bit;
    }

    public final void clearBit(long address) {
        computeBits(address);
        if (t_blockNumber >= arraySize) {
            increaseArraySize();
        }

        if (m_array[t_blockNumber] == null) {
            m_array[t_blockNumber] = new int[m_blockSize];
        }
        t_inverseBit = ~t_bit;
        m_array[t_blockNumber][t_wordNumber] &= t_inverseBit;
    }

    /*
        computes the three basic parts, block, word, bit
        Yes there is an awefull lot of casting from long to int
        this could be a problem when the long is.
        I am carefull at least to use longs to the point I have
        extracted the appropriate bits and then cast them to the appropriate int.
    */
    private final void computeBits(long address) {
        t_wordNumber = (int) (address >> 5);
        // now get block offset
        t_blockNumber = (int) (address >> blockSizeToPowerPlusBitOffset);

        t_wordNumber = t_wordNumber & wordMask;
        t_bitNumber = (int) address & 0x1F;
        t_bit = 1 << t_bitNumber;
    }

    private final int createWordMask(int bitsize) {
        int result = 1;
        for (int i = 0; i < bitsize; i++) {
            result = result << 1;
            result = result | 1;
        }
        return result;
    }

    public final void clearAll() {
        for (int i = 0; i < m_array.length; i++) {
            if (m_array[i] != null) {
                SparseBitVectorBlockPool.freeBlock(blockSizeToPower, m_array[i]);
                m_array[i] = null;
            }
        }
    }

    private final void increaseArraySize() {
        int[] temp[] = new int[t_blockNumber + 1][];
        copyArray(m_array, temp, arraySize);
        arraySize = t_blockNumber + 1;
        m_array = temp;
    }

    private void copyArray(int[][] from, int[][] too, int count) {
        for (int i = 0; i < count; i++) {
            too[i] = from[i];
        }
    }
}
