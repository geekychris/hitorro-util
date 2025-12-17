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

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.text.ParseException;

/**
 * bundle of commonly used constants, such as time translations.
 * <p/>
 * Also includes some flyweights (int and long)
 *
 * @author ccollins
 */
public class Constants {
    public static final String APPLICATION_JSON = "application/json";
    public static final String UTF8 = "UTF-8";

    public static final int ActiveState = 1;
    public static final int ascii_0 = 48;


    public static final int ascii_1 = 49;

    public static final int ascii_2 = 50;

    public static final int ascii_3 = 51;

    public static final int ascii_4 = 52;

    public static final int ascii_5 = 53;

    public static final int ascii_6 = 54;

    public static final int ascii_7 = 55;

    public static final int ascii_8 = 56;

    public static final int ascii_9 = 57;


    public static final int ascii_a = 97;

    public static final int ascii_A = 65;

    public static final char[] ascii_alpaha = {};

    public static final int ascii_b = 98;

    public static final int ascii_B = 66;

    public static final int ascii_c = 99;

    public static final int ascii_C = 67;

    public static final int ascii_d = 100;

    public static final int ascii_D = 68;

    public static final int ascii_e = 101;

    public static final int ascii_E = 69;

    public static final int ascii_f = 102;

    public static final int ascii_F = 70;

    public static final int ascii_g = 103;

    public static final int ascii_G = 71;

    public static final int ascii_h = 104;
    public static final int ascii_H = 72;
    public static final int ascii_i = 105;
    public static final int ascii_I = 73;
    public static final int ascii_j = 106;
    public static final int ascii_J = 74;
    public static final int ascii_k = 107;
    public static final int ascii_K = 75;
    public static final int ascii_l = 108;
    public static final int ascii_L = 76;
    public static final int ascii_m = 109;
    public static final int ascii_M = 77;
    public static final int ascii_n = 110;
    public static final int ascii_N = 78;
    public static final char[] ascii_numeric = {ascii_0, ascii_1, ascii_2, ascii_3, ascii_4, ascii_5, ascii_6, ascii_7, ascii_8, ascii_9};
    public static final int ascii_o = 111;
    public static final int ascii_O = 79;
    public static final int ascii_p = 112;
    public static final int ascii_P = 80;
    public static final int ascii_q = 113;
    public static final int ascii_Q = 81;
    public static final int ascii_r = 114;
    public static final int ascii_R = 82;
    public static final int ascii_s = 115;
    public static final int ascii_S = 83;
    public static final int ascii_t = 116;
    public static final int ascii_T = 84;
    public static final int ascii_u = 117;
    public static final int ascii_U = 85;
    public static final int ascii_v = 118;
    public static final int ascii_V = 86;
    public static final int ascii_w = 119;
    public static final int ascii_W = 87;
    public static final int ascii_x = 120;
    public static final int ascii_X = 88;
    public static final int ascii_y = 121;
    public static final int ascii_Y = 89;
    public static final int ascii_z = 122;
    public static final int ascii_Z = 90;
    public static final String CarriageReturn = "\r";
    public static final String CarriageReturnLineFeed = "\r\n";
    // String constants
    public static final String EmptyString = "";
    public static final int HoursInDay = 24;
    // object states
    public static final int InactiveState = 0;
    public static final int KBytes = 1024;
    public static final long MBytes = KBytes * KBytes;
    public static final long GBytes = MBytes * KBytes;
    public static final long MillisInSecond = 1000;
    public static final long MillisInMinute = 60 * MillisInSecond;
    public static final long MillisInHour = 60 * MillisInMinute;
    public static final int MinutesInHour = 60;
    public static final long MillisInDay = MillisInMinute * MinutesInHour * HoursInDay;
    public static final long MillisInWeek = MillisInDay * 7;
    public static final long MillisInMonth = MillisInWeek * 4;
    public static final long MillisInYear = MillisInDay * 365;

    public static final int SecondsInMinute = 60;
    public static final int SecondsInHour = SecondsInMinute * 60;
    public static final int MinutesInDay = MinutesInHour * 24;
    public static final char NewLineChar = '\n';
    public static final String NewLineString = "\n";
    public static final String SystemNewLine = System.getProperty("line.separator");

    // time constants
    public static final char TabLineChar = '\t';
    public static final char DoubleQuoteChar = '"';
    public static final String XMLMimeType = "text/xml";
    public static final String HTMLMimeType = "text/html";// known folder names
    public static final String PendingRssInputFeedsFolder = "Pending Rss In Feeds";
    private static final int IntegersToPreCompute = 100;
    private static final int LongsToPreCompute = 100;
    private static final int doubleToPrecompute = 1000;
    public static char[] NULL_CHAR_ARRAY = {'\u0000'};
    public static String NULL_ENDING_STRING = new String(NULL_CHAR_ARRAY, 0, 1);
    public static String SPACE = " ";
    public static ListValue[] s_stateListValues;
    private static Integer[] s_integerFlyweight = calculateIntFlyweight();
    private static Long[] s_longFlyweight = calculateLongFlyweight();

    private static Integer[] calculateIntFlyweight() {

        // Integers
        Integer in[] = new Integer[IntegersToPreCompute];
        for (int i = 0; i < IntegersToPreCompute; i++) {
            in[i] = Integer.valueOf(i);
        }

        return in;
    }

    private static Long[] calculateLongFlyweight() {
        // Longs
        Long lo[] = new Long[LongsToPreCompute];
        for (int i = 0; i < LongsToPreCompute; i++) {
            lo[i] = Long.valueOf(i);
        }
        return lo;
    }

    public static final String getMillisToPrettyForm(long millis) {
        int seconds = (int) (millis / 1000);
        return Fmt.S("%s:%s:%s", seconds / (Constants.SecondsInHour), (seconds / (Constants.SecondsInMinute)) % 60, seconds % 60);
    }

    public static final String getSecondsToPrettyForm(int seconds) {
        return Fmt.S("%s:%s:%s", seconds / (Constants.SecondsInHour), (seconds / (Constants.SecondsInMinute)) % 60, seconds % 60);
    }

    public static final boolean getBool(String b) {
        if (StringUtil.nullOrEmptyString(b)) {
            return false;
        }
        char c = b.charAt(0);
        c = Character.toLowerCase(c);
        switch (c) {
            case '1':
            case 't':
            case 'y':
                return true;
            default:
                return false;
        }
    }


    /*
     * Optimize use of integers to avoid newing up unecessary Integers if we are
     * using the common numbers (0-99)
     */

    public static Boolean getBoolean(boolean flag) {
        if (flag) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    /**
     * Object version of getBool.
     *
     * @return Boolean.TRUE if starts with 1|t|T|y|Y otherwise Boolean.FALSE.
     */
    public static final Boolean getBoolean(String b) {
        return Boolean.valueOf(getBool(b));
    }

    /**
     * Takes a string of the form:
     * <p/>
     * 1K 1M 1G and translates these into kbytes, mbytes and gbytes
     *
     * @param parseMe
     * @return
     * @throws ParseException
     */
    public static final long getBytesFromString(String parseMe)
            throws ParseException {
        if (!StringUtil.nullOrEmptyString(parseMe)) {
            parseMe = parseMe.toLowerCase();
            char c = parseMe.charAt(parseMe.length() - 1);
            if (Character.isDigit(c)) {
                return Long.parseLong(parseMe);
            }
            String val = parseMe.substring(0, parseMe.length() - 1);
            long valInt = Long.parseLong(val);
            switch (c) {
                case 'b':
                    return valInt * KBytes;
                case 'm':
                    return valInt * MBytes;
                case 'g':
                    return valInt * GBytes;
                default:
                    throw new ParseException(Fmt.S(
                            "Unable to parse %s unknown control code %s", parseMe,
                            c), -1);

            }
        }
        throw new ParseException("Unable to parse null", -1);
    }

    /**
     * Pretty print the number of bytes in the form of a bytes as B,K,M,G
     *
     * @param bytes
     * @return string of the form 45MB
     */
    public static final String getBytesNeatForm(long bytes) {
        if (bytes < KBytes) {
            return Fmt.S("%sB", bytes);
        }
        if (bytes < MBytes) {
            return Fmt.S("%sKB", bytes / KBytes);
        }
        if (bytes < GBytes) {
            return Fmt.S("%sMB", bytes / MBytes);
        }
        return Fmt.S("%sGB", bytes / GBytes);
    }

    public static final Integer getInteger(int i) {
        if (i < IntegersToPreCompute && i >= 0) {
            return s_integerFlyweight[i];
        }
        return Integer.valueOf(i);
    }

    /**
     * Optimize use of Longs to avoid newing up unecessary Integers if we are using the common numbers (0-99)
     */

    public static final Long getLong(long l) {
        if (l < LongsToPreCompute && l >= 0) {
            return s_longFlyweight[(int) l];
        }
        return Long.valueOf(l);
    }

    /**
     * Calculate the amount of millis that transpires based upon days, hours and minutes
     *
     * @param days
     * @param hours
     * @param minutes
     * @return
     */
    public static final long getMillis(int days, int hours, int minutes, int seconds) {
        return (MillisInDay * days) + (MillisInHour * hours) + (MillisInMinute * minutes) + (MillisInSecond * seconds);
    }

    public static final boolean isNumber(String s) {
        int size = s.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Safely parse an integer, returning 0 if can't be parsed.
     *
     * @param sval The string to be parsed.
     * @return the integer value of the string, or 0 if it can't be parsed
     */
    public static int safeParseInt(String sval) {
        int result = 0;
        try {
            result = Integer.parseInt(sval);
        } catch (NumberFormatException exc) {
            result = 0;
        }

        return result;
    }

    /**
     * Get standard listvalues for the states. These can be used for displaying states in select lists anywhere in the
     * ui.
     *
     * @return ListValues for all the states
     */
    public static ListValue[] getStateListValues() {
        if (s_stateListValues == null) {
            s_stateListValues = new ListValue[2];
            s_stateListValues[0] = new ListValue("Active", ActiveState);
            s_stateListValues[1] = new ListValue("Inactive", InactiveState);
        }
        return s_stateListValues;
    }

}
