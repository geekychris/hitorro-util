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

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import net.wimpi.telnetd.io.BasicTerminalIO;

import java.io.IOException;

/**
 * User: chris
 */
public class TelnetResponse extends Response {
    private static final char PadChar = ' ';
    StringBuilder m_builder = new StringBuilder();
    private boolean endCalled = false;
    private BasicTerminalIO bio;
    private int paddingFromHeader;
    private int columnSize[];
    private String columnSeperator = "";
    private String endOfLineSeperator = "";
    private int rowNumber = 0;

    public TelnetResponse(BasicTerminalIO bio) {
        this.bio = bio;
    }

    public void setCommandSession(CommandSession cs) {
        columnSeperator = cs.getVarAsString("columseperator");
        columnSeperator = StringUtil.ifNullOrEmptyReplace(columnSeperator, "");
        endOfLineSeperator = cs.getVarAsString("rowseperator");
        endOfLineSeperator = StringUtil.ifNullOrEmptyReplace(endOfLineSeperator, "");
    }

    @Override
    public void addBannerRow(String row) {
        try {
            bio.setForegroundColor(BasicTerminalIO.GREEN);
            bio.write(row);
            bio.setForegroundColor(BasicTerminalIO.BLACK);
            bio.write(Constants.CarriageReturnLineFeed);
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }

    }

    public void addInfo(InfoLevel level, String info) {
        try {
            bio.write(Constants.CarriageReturnLineFeed);
            bio.setForegroundColor(level.getColor());
            bio.write(level.name());
            bio.setForegroundColor(BasicTerminalIO.BLACK);
            bio.write(info);
            bio.write(Constants.CarriageReturnLineFeed);
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        try {
            if (percentComplete != -1) {
                bio.write(Fmt.S("%s - %s", percentComplete, info));
            } else {
                bio.write(info);
            }
            bio.write(Constants.CarriageReturnLineFeed);
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }

    public void setResponseShape(ResponseShape shape) {
        super.setResponseShape(shape);
        addHeaderArray(shape.m_header.getHeader());
    }

    public void addHeader(String... headers) {
        addHeaderArray(headers);
    }

    public void addHeaderArray(String headers[]) {
        columnSize = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            Object o = headers[i];
            int l = m_builder.length();
            StringUtil.pad(o, PadChar, paddingFromHeader, m_builder);
            columnSize[i] = m_builder.length() - l;
        }
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    @Override
    public void addRowArray(Object elements[]) {
        try {
            int min = Math.min(elements.length, columnSize.length);
            for (int i = 0; i < min; i++) {
                Object o = elements[i];
                if (i > 0) {
                    bio.write(columnSeperator);
                }
                if (rowNumber == 0) {
                    bio.setForegroundColor(BasicTerminalIO.BLUE);

                } else {
                    RenderingContainer.setTerminal(this.containers, i, bio);
                }

                bio.write(StringUtil.gePadding(o, PadChar, columnSize[i], m_builder));
                if (rowNumber == 0) {
                    bio.setForegroundColor(BasicTerminalIO.BLACK);
                    bio.setBackgroundColor(BasicTerminalIO.COLORINIT);
                }


            }
            rowNumber++;
            bio.write(endOfLineSeperator);
            bio.write(Constants.CarriageReturnLineFeed);
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }

    @Override
    public void end() {
        if (endCalled) {
            return;
        }
        endCalled = true;
        rowNumber = 0;
        try {

            bio.flush();
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }
}
