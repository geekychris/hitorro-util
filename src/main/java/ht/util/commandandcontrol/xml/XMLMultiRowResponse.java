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
package ht.util.commandandcontrol.xml;

import ht.util.commandandcontrol.MultiRowResponse;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseShape;
import ht.util.core.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
class XMLMultiRowResponse extends MultiRowResponse {
    private static AttributesImpl EmptyAttributes = new AttributesImpl();

    XMLResponse resp;
    private int groupOffset = -1;
    private String groupName;

    XMLMultiRowResponse(int columns, XMLResponse response, ResponseShape shape) {
        super(columns, shape);
        resp = response;
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
    }

    public void addTupleArray(int offset, Object elems[]) {
        try {
            if (offset == 0) {
                resp.writer.startElement("", shape.getRowName(), "", EmptyAttributes);
            }

            String tupleName = shape.getGroups()[offset].getShortName();
            if (groupOffset != offset) {
                closeGroup();
                groupOffset = offset;
                groupName = shape.getGroups()[offset].getSetName();
                resp.writer.startElement("", groupName, "", EmptyAttributes);
            }

            resp.writer.startElement("", tupleName, "", EmptyAttributes);

            for (int i = 0; i < elems.length; i++) {
                add(offset + i, elems[i]);
            }
            resp.writer.endElement("", tupleName, "");
        } catch (SAXException e) {
            Log.util.error("Exception %s %e", e, e);
        }
    }

    private void closeGroup() throws SAXException {
        if (groupOffset != -1) {
            resp.writer.endElement("", groupName, "");
        }
    }

    public void addThrowable(int column, Throwable t, int stackDepth, int startFrom) {
        if (t == null) {
            return;
        }
        StackTraceElement[] elements = t.getStackTrace();
        stackDepth = Math.min(stackDepth + startFrom, elements.length);
        for (int i = startFrom; i < stackDepth; i++) {
            add(column, elements[i].toString());
        }
    }

    public void clear() {
        // do nothing
    }

    public boolean add(int index, Object o) {
        try {
            if (index == 0) {
                AttributesImpl attributes = new AttributesImpl();
                resp.writer.startElement("", shape.getRowName(), "", EmptyAttributes);
            }
            String name = shape.getHeaderShort()[index];
            // here we could put type in the attribute
            resp.writer.startElement("", name, "", EmptyAttributes);
            if (o != null) {
                resp.writer.characters(o.toString());
            }

            resp.writer.endElement("", name, "");

        } catch (SAXException e) {
            Log.util.error("Exception %s %e", e, e);
        }

        return true;
    }

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public void addToResponse(Response response) {
        // Do nothing
        try {
            closeGroup();
            resp.writer.endElement("", shape.getRowName(), "");
        } catch (SAXException e) {
            Log.util.error("Exception %s %e", e, e);
        }

    }
}
