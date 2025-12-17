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
package ht.util.commandandcontrol;

import ht.util.core.string.Fmt;
import net.wimpi.telnetd.io.BasicTerminalIO;

import java.io.IOException;

/**
 * User: chris
 */
public class RenderingContainer {
    public ColorEnum bgColor = null;
    public ColorEnum fontColor = null;

    public RenderingContainer() {


    }

    public RenderingContainer(ColorEnum bgColor, ColorEnum fontColor) {
        this.bgColor = bgColor;
        this.fontColor = fontColor;
    }

    public static String renderForHtml(RenderingContainer rcArr[], int offset, Object values[]) {
        if (values == null || values[offset] == null) {
            return Fmt.S("<td>%s</td>", "");
        }

        if (rcArr == null || offset >= rcArr.length || rcArr[offset] == null) {
            return Fmt.S("<td>%s</td>", values[offset].toString());
        }

        return renderForHtml(rcArr, offset, values[offset].toString());
    }

    public static void setForTerminal(RenderingContainer rcArr[], int offset, BasicTerminalIO btio) throws IOException {
        setTerminal(rcArr, offset, btio);
    }

    public static String renderForHtml(RenderingContainer rcArr[], int offset, Object value) {
        if (rcArr == null || offset >= rcArr.length || rcArr[offset] == null) {
            return Fmt.S("<td>%s</td>", value.toString());

        }

        if (rcArr[offset].bgColor != null) {
            return Fmt.S("<td BGCOLOR=\"%s\">%s</td>", rcArr[offset].bgColor.getHTMLColorString(),
                    rcArr[offset].getFontified(rcArr[offset], value.toString()));
        } else {
            return Fmt.S("<td>%s</td>", rcArr[offset].getFontified(rcArr[offset], value.toString()));
        }
    }

    public static void setTerminal(RenderingContainer rcArr[], int offset, BasicTerminalIO btio) throws IOException {
        if (rcArr == null || offset >= rcArr.length || rcArr[offset] == null) {
            return;

        }

        if (rcArr[offset].bgColor != null) {
            btio.setBackgroundColor(rcArr[offset].bgColor.getTerminalColor());
        }

        if (rcArr[offset].fontColor != null) {
            btio.setForegroundColor(rcArr[offset].fontColor.getTerminalColor());
        }

    }

    private String getFontified(RenderingContainer rc, String value) {
        if (rc.fontColor != null) {
            return Fmt.S("<font color=\"%s\">%s</font>", rc.fontColor.getHTMLColorString(), value);
        } else {
            return value;
        }
    }
}
