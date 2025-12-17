package ht.util.core;

import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class IntegerUtil {
    public static final int Mask = 0xff;

    /**
     * convert any old number into -1, 0, 1
     * <p>
     * examples:
     * -111 -> -1
     * 789  ->  1
     * 0  ->  0
     *
     * @param value
     * @return
     */
    public static final int translateMagnatudeToOrdinal(int value) {
        if (value > 0) {
            return 1;
        }
        if (value < 0) {
            return -1;
        }
        return 0;
    }

    /**
     * Ellipses form of Math.max()
     *
     * @param values
     * @return
     */
    public static final int maxValue(int... values) {
        int max = Integer.MIN_VALUE;
        for (int i : values) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    /**
     * Ensure a string is infact a number!.
     *
     * @param test
     * @return true if just digits.
     */
    public static final boolean isNumber(String test) {
        if (StringUtil.nullOrEmptyString(test)) {
            return false;
        }
        int size = test.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(test.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static final boolean isNumber(char test) {
        return (test >= Constants.ascii_0 && test <= Constants.ascii_9);
    }

    public static final int charNumbertoInt(char num) {
        return (num - Constants.ascii_0);
    }

    /**
     * Parse the first integer in this string, skipping any initial non-digit characters and ending with the first
     * non-digit character after the number
     *
     * @param string
     * @return the integer
     */
    public static final int parseInt(String string)
            throws NumberFormatException {
        int begin = -1;
        int end = string.length();

        for (int i = 0; i < end; i++) {
            boolean isDigit = Character.isDigit(string.charAt(i));
            if (begin < 0 && isDigit) {
                begin = i;
            } else if (begin >= 0 && !isDigit) {
                end = i;
                break;
            }
        }
        if (begin < 0) {
            throw new NumberFormatException(string);
        }

        return Integer.parseInt(string.substring(begin, end));
    }

    public static final int getShortAsHighPartOfInt(short s) {
        return ((s & 0xff00) >> 8) - 127;
    }

    public static final int getShortAsLowPartOfInt(short s) {
        return (s & 0xff) - 127;
    }

    public static short combineSmallIntsAsShort(int a, int b) {
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
