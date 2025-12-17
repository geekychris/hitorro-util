package ht.util.core;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 28, 2005 Time: 10:32:17 AM Utils for
 * combining bytes and ints, etc into a single unit (such as long)
 */
public class ByteManipulationUtil {
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
}
