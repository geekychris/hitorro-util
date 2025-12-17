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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.core.string.StringUtil;
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
