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
package com.hitorro.util.commandandcontrol.xml;

import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.core.Console;
import org.xml.sax.helpers.AttributesImpl;



public class HTMLMultiRowResponse extends com.hitorro.util.commandandcontrol.MultiRowResponse {
    private static AttributesImpl EmptyAttributes = new AttributesImpl();

    HTMLResponse resp;
    private int groupOffset = -1;
    private String groupName;

    HTMLMultiRowResponse(int columns, HTMLResponse response, com.hitorro.util.commandandcontrol.ResponseShape shape) {
        super(columns, shape);
        resp = response;
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
        //containers = null;
    }

    public void addTupleArray(int offset, Object elems[]) {

        if (groupOffset != offset) {
            closeGroup();
            groupOffset = offset;
            com.hitorro.util.commandandcontrol.GroupTuple gts[] = shape.getGroups();
            Console.println(resp.writer, "<TD><TABLE valign=top align=left border=1><TR>");
            for (int i = 0; i < gts[offset].getSize(); i++) {
                Console.print(resp.writer, "<TH>%s</TH>", shape.getHeaderLong()[offset + i]);
            }
            Console.println(resp.writer, "</TR>");
        }

        //resp.writer.startElement("", tupleName, "", EmptyAttributes);
        Console.println(resp.writer, "<TR>");
        for (int i = 0; i < elems.length; i++) {
            add(offset + i, elems[i]);
        }
        Console.println(resp.writer, "</TR>");

    }

    private void closeGroup() {
        if (groupOffset != -1) {
            //Console.println(resp.writer, "</TABLE  border=\"1\"></TD>");
            Console.println(resp.writer, "</TABLE></TD>");
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
        if (o != null) {
            Console.println(resp.writer, com.hitorro.util.commandandcontrol.RenderingContainer.renderForHtml(this.containers, index, o));
        }

        return true;
    }

    public boolean add(int index, Object[] arr) {
        String name = shape.getHeaderShort()[index];

        if (arr[index] != null) {
            Console.println(resp.writer, com.hitorro.util.commandandcontrol.RenderingContainer.renderForHtml(this.containers, index, arr));
        }

        return true;
    }

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public void addToResponse(com.hitorro.util.commandandcontrol.Response response) {
        closeGroup();
        Console.println(resp.writer, "</TR>");
    }
}
