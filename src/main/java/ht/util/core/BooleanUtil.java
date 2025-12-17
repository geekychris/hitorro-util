package ht.util.core;

import ht.util.core.string.StringUtil;

/**
 * @author chris
 */
public class BooleanUtil {

    public static final String getTrueFalseFlag(boolean flag) {
        if (flag) {
            return "t";
        }
        return "f";
    }

    public static final String getYNFlag(boolean flag) {
        if (flag) {
            return "y";
        }
        return "n";
    }


    /**
     * parse a string and determine if its a true or a false....its pretty lame, we only look at the first character.
     * Also we assume that an empty or null string is false;
     *
     * @param bIn
     * @return
     */
    public static final boolean getBoolean(String bIn) {
        if (StringUtil.nullOrEmptyString(bIn)) {
            return false;
        }
        char c = bIn.charAt(0);
        switch (c) {
            case 't':
            case 'T':
            case '1':
            case 'y':
                return true;
            default:
                return false;
        }
    }

    /**
     * Check whether a String is a valid Boolean.
     *
     * @param bIn The string to check
     * @return true if the string is a valid Boolean representation
     */
    public static final boolean isBoolean(String bIn) {
        if (StringUtil.nullOrEmptyOrBlankString(bIn)) {
            return false;
        }

        char c = bIn.charAt(0);
        switch (c) {
            case 't':
            case 'T':
            case '1':
            case 'f':
            case 'F':
            case '0':
            case 'y':
            case 'n':
                return true;
            default:
                return false;
        }
    }


    /**
     * Get an array of boolean.
     *
     * @param size  - number of elements in the array
     * @param value - default value for each arrya element, true or false
     * @return initialized array of size, size.
     */
    public static final boolean[] getBooleanArray(int size, boolean value) {
        if (size < 0) {
            size = 0;
        }

        boolean[] boolArray = new boolean[size];

        for (boolean element : boolArray) {
            element = value;
        }

        return boolArray;

    }
}
