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

/**
 */
public class TeeResponse extends Response {
    private Response m_left;
    private Response m_right;

    public TeeResponse(Response left, Response right) {
        m_left = left;
        m_right = right;
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        m_left.addStatusUpdateMessage(info, percentComplete);
        m_right.addStatusUpdateMessage(info, percentComplete);
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        m_left.setResponseShape(s);
        m_right.setResponseShape(s);
    }

    public void setCommandSession(CommandSession sess) {
        m_left.setCommandSession(sess);
        m_right.setCommandSession(sess);
    }

    public void addBannerRow(String row) {
        m_left.addBannerRow(row);
        m_right.addBannerRow(row);
    }

    public void addRow(Object... elements) {
        m_left.addRow(elements);
        m_right.addRow(elements);
    }

    public void addRowArray(Object elements[]) {
        m_left.addRowArray(elements);
        m_right.addRowArray(elements);
    }

    public void addInfo(InfoLevel level, String info) {
        m_left.addInfo(level, info);
        m_right.addInfo(level, info);
    }

    public void end() {
        m_left.end();
        m_right.end();
    }
}
