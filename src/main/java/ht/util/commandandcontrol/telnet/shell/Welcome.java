package ht.util.commandandcontrol.telnet.shell;

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
    public static final String[] getPicture(final int right) {
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
