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
package com.hitorro.util.commandandcontrol.telnet.shell;

import java.util.Random;

public class Welcome {


    private static final String[] bull = new String[]{
            "       (_)",
            "    _ / \",",
            " ./( (  )",
            "   f^~Y^|",
            "   ~  ^ ^"
    };

    private static final StringBuffer spaces = new StringBuffer("                   ");


    /**
     * Get maximum length of <code>String</code> array.
     *
     * @param arg Array to work on. Must not be <code>null</code>.
     * @return Maximum length. Maybe <code>-1</code> if all entries are <code>null</code>.
     */
    public static final int getMaximumLength(final String[] arg) {
        int maximum = -1;
        for (int i = 0; i < arg.length; i++) {
            if (arg[i] != null && arg[i].length() > maximum) {
                maximum = arg[i].length();
            }
        }
        return maximum;
    }

    /**
     * Get required number of spaces.
     *
     * @param length Number of spaces wanted.
     * @return Spaces.
     */
    public static final String getSpaces(final int length) {
        while (length > spaces.length()) {
            spaces.append(spaces);
        }
        return spaces.substring(0, length);
    }

    /**
     * Get random picture string.
     *
     * @param right Right edge position in characters.
     * @return Right aligned ascii picture. Each <code>String</code> is already terminated by <code>\r\n</code>.
     */
    public static String[] getPicture(final int right) {
        final Random random = new Random();
        final String[] welcome = bull;
        final String[] result = new String[welcome.length];
        final String spaces = Welcome.getSpaces(right - getMaximumLength(welcome) - 1);
        for (int i = 0; i < welcome.length; i++) {
            result[i] = spaces + welcome[i] + "\r\n";
        }
        return result;
    }

}
