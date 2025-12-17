package ht.util.commandandcontrol;

import ht.util.core.string.StringUtil;
import net.wimpi.telnetd.io.BasicTerminalIO;

/**
 * User: chris
 */
public enum ColorEnum {
    red(0xff0000, BasicTerminalIO.RED),
    green(0x00ff00, BasicTerminalIO.GREEN),
    blue(0x0000ff, BasicTerminalIO.BLUE),
    turquoise(0x00ffff, BasicTerminalIO.CYAN),
    yellow(0xffff00, BasicTerminalIO.YELLOW);

    private int htmlColor;
    private String htmlColorString;
    private int terminalColor;

    ColorEnum(int htmlColor, int terminalColor) {
        this.htmlColor = htmlColor;
        this.terminalColor = terminalColor;
        htmlColorString = packColor(htmlColor);
    }

    private static String packColor(int col) {

        String str = Integer.toHexString(col);
        if (str.length() == 2) {
            return StringUtil.strcat("0000", str);
        } else if (str.length() == 4) {
            return StringUtil.strcat("00", str);
        }
        return str;
    }

    public int getHTMLColor() {
        return htmlColor;
    }

    public int getTerminalColor() {
        return terminalColor;
    }

    public String getHTMLColorString() {
        return htmlColorString;
    }
}
