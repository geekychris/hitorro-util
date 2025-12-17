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
package com.hitorro.util.commandandcontrol.serialized;

import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
class HTSerializedMultiRowResponse extends com.hitorro.util.commandandcontrol.MultiRowResponse {
    private HTSerializedResponse resp;
    private String names[];
    private List<Object> objects[];

    HTSerializedMultiRowResponse(int columns, HTSerializedResponse response, com.hitorro.util.commandandcontrol.ResponseShape shape) {
        super(columns, shape);
        names = new String[shape.getHeaderShort().length];
        objects = new List[shape.getHeaderShort().length];
        resp = response;
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
    }

    public void addTupleArray(int offset, Object elems[]) {
        List<Object> list = getList(offset, true);
        com.hitorro.util.commandandcontrol.ResponseTuple rt = new com.hitorro.util.commandandcontrol.ResponseTuple();
        String names[] = new String[elems.length];
        com.hitorro.util.commandandcontrol.GroupTuple gt = shape.getGroups()[offset];
        rt.setTupleName(gt.getShortName());
        for (int i = 0; i < elems.length; i++) {
            names[i] = shape.getHeaderShort()[offset + i];
        }
        rt.setNames(names);

        rt.setValues(StringUtil.objectArrayToString(elems, ""));
        list.add(rt);

    }

    private List<Object> getList(int offset, boolean tuple) {
        List<Object> list = objects[offset];
        if (list == null) {
            list = new ArrayList<Object>();
            objects[offset] = list;
            if (tuple) {
                names[offset] = shape.getGroups()[offset].getShortName();
            } else {
                names[offset] = shape.getHeaderShort()[offset];
            }

        }
        return list;
    }

    public void addThrowable(int column, Throwable t, int stackDepth, int startFrom) {
        Console.println("addThrowable, %s", column);
    }

    public void clear() {
        for (List l : objects) {
            if (l != null) {
                l.clear();
            }
        }
    }

    public boolean add(int index, Object o) {
        List<Object> list = getList(index, false);
        if (o == null) {
            list.add("");
        } else {
            list.add(o.toString());
        }
        return true;
    }

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public void addToResponse(com.hitorro.util.commandandcontrol.Response response) {
        com.hitorro.util.commandandcontrol.Row r = new com.hitorro.util.commandandcontrol.Row();
        int rowCount = getRowCount();
        String n[] = new String[rowCount];
        getNames(n);

        r.setNames(n);
        Object ro[][] = new Object[rowCount][];
        getRow(ro);
        r.setFromTuples(ro);
        resp.addToResponse(r);
    }

    private int getRowCount() {
        int count = 0;
        for (List l : objects) {
            if (l != null && l.size() > 0) {
                count++;
            }
        }
        return count;
    }

    private void getNames(String n[]) {
        int count = 0;
        for (int i = 0; i < objects.length; i++) {
            List l = objects[i];
            if (l != null && l.size() > 0) {
                n[count] = this.names[i];
                count++;
            }
        }
    }

    private void getRow(Object n[][]) {
        int count = 0;
        for (int i = 0; i < objects.length; i++) {
            List l = objects[i];
            if (l != null) {
                int size = l.size();
                if (size > 0) {
                    n[count] = new Object[size];
                    for (int j = 0; j < size; j++) {
                        n[count][j] = l.get(j);
                    }
                    count++;
                }
            }
        }
    }

}
