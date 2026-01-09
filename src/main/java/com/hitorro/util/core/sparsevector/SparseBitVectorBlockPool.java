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

import java.util.Arrays;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 28, 2004 Time: 2:08:24 PM
 * <p/>
 * Description:
 * <p/>
 * Memory pool for blocks used by the SparseBitVector.
 */
public class SparseBitVectorBlockPool {
    private static SparseBitVectorBlockPoolNode[] nodeChains = new SparseBitVectorBlockPoolNode[32];
    private static Object lock = new Object();

    public static final SparseBitVector getBitVector(int blockSizeToPower, int initialArraySize) {
        SparseBitVector sbv = new SparseBitVector(blockSizeToPower, initialArraySize);
        return sbv;
    }

    public static int[] getBlock(int addressBitSize) {
        synchronized (lock) {
            if (nodeChains[addressBitSize] == null) {
                return getBlockNew(addressBitSize);
            }
            SparseBitVectorBlockPoolNode node = nodeChains[addressBitSize];
            nodeChains[addressBitSize] = node.m_next;
            return node.m_block;
        }
    }

    private static int[] getBlockNew(int addressBitSize) {
        return new int[addressBitSize = 1 << addressBitSize];
    }

    public static void freeBlock(int blockSizePower, int[] block) {
        Arrays.fill(block, 0);
        synchronized (lock) {
            nodeChains[blockSizePower] =
                    new SparseBitVectorBlockPoolNode(block, nodeChains[blockSizePower]);
        }
    }
}

class SparseBitVectorBlockPoolNode {
    public int[] m_block;
    public SparseBitVectorBlockPoolNode m_next;
    public SparseBitVectorBlockPoolNode(int[] block, SparseBitVectorBlockPoolNode next) {
        m_block = block;
        m_next = next;
    }
}
