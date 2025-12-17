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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Basic response object for spewing out the response to a debug command.
 *
 * @author chris
 */
public abstract class Response {
    protected ResponseShape shape;

    protected HttpServletResponse httpResponse;

    protected HttpServletRequest httpRequest;
    protected RenderingContainer containers[];

    public HttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpServletRequest r) {
        httpRequest = r;
    }

    public HttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpServletResponse r) {
        httpResponse = r;
    }

    public void setCommandSession(CommandSession sess) {
        // do nothing / others may wish to pull session variables out
    }

    /**
     * @param row
     */
    public abstract void addBannerRow(String row);

    public void setRenderingRow(RenderingContainer... containers) {
        this.containers = containers;
    }

    public abstract void addRow(Object... elements);

    public abstract void addRowArray(Object elements[]);

    public abstract void addInfo(InfoLevel level, String info);

    /**
     * unlike other messages, this one is not buffered and is output the the recipient.  Useful for updating the
     * progress of a computation.
     *
     * @param info
     */
    public abstract void addStatusUpdateMessage(String info, int percentComplete);

    public abstract void end();


    public ResponseShape getShape() {
        return shape;
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        shape = s;
    }

    public MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new StandardMultiRowResponse(shape.m_headerShort.length, shape);
    }

    public void addMultiRowResponse(MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }
}
