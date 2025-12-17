package ht.util.core.sparsevector;

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
    private static SparseBitVectorBlockPoolNode[] m_nodeChains = new SparseBitVectorBlockPoolNode[32];
    private static Object lock = new Object();

    public static final SparseBitVector getBitVector(int blockSizeToPower, int initialArraySize) {
        SparseBitVector sbv = new SparseBitVector(blockSizeToPower, initialArraySize);
        return sbv;
    }

    public static int[] getBlock(int addressBitSize) {
        synchronized (lock) {
            if (m_nodeChains[addressBitSize] == null) {
                return getBlockNew(addressBitSize);
            }
            SparseBitVectorBlockPoolNode node = m_nodeChains[addressBitSize];
            m_nodeChains[addressBitSize] = node.m_next;
            return node.m_block;
        }
    }

    private static int[] getBlockNew(int addressBitSize) {
        return new int[addressBitSize = 1 << addressBitSize];
    }

    public static void freeBlock(int blockSizePower, int[] block) {
        Arrays.fill(block, 0);
        synchronized (lock) {
            m_nodeChains[blockSizePower] =
                    new SparseBitVectorBlockPoolNode(block, m_nodeChains[blockSizePower]);
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
