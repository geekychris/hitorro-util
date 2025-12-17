package ht.util.core;

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


