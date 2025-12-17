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
package ht.util.commandandcontrol.serialized;


import ht.util.commandandcontrol.*;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */

public class HTSerializedResponse extends Response {
    private List<InfoRow> list;

    //
    public HTSerializedResponse(List<InfoRow> list) {
        this.list = list;
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getShortNames());
    }

    public void addBannerRow(String row) {
    }

    public void addHeaderArray(String columnHeaders[]) {
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }


    public void addRowArray(Object elements[]) {
        Row rr = new Row();
        rr.setNames(this.shape.getShortNames());
        rr.setRow(elements);
        addToResponse(rr);
    }


    public void addInfo(InfoLevel level, String info) {
        addToResponse(new InfoRow(level.name(), info));
    }

    public void end() {
    }

    public MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new HTSerializedMultiRowResponse(shape.getShortNames().length, this, this.shape);
    }

    public void addMultiRowResponse(MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }

    void addToResponse(InfoRow r) {
        list.add(r);
    }
}

