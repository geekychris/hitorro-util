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
package com.hitorro.util.commandandcontrol.json;

import com.hitorro.util.commandandcontrol.GroupTuple;
import com.hitorro.util.commandandcontrol.MultiRowResponse;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.JSONList;
import com.hitorro.util.json.JSONMap;

import java.io.IOException;


public class JSONMultiRowResponse extends MultiRowResponse {
    private JSONResponse resp;
    private Class classes[];
    private JSONMap map = new JSONMap();

    JSONMultiRowResponse(int columns, JSONResponse response, ResponseShape shape) {
        super(columns, shape);
        resp = response;
        classes = resp.getShape().getClasses();
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
    }

    public void addTupleArray(int offset, Object elems[]) {
        GroupTuple gt = shape.getGroups()[offset];
        int size = elems.length;
        if (gt != null) {
            // we have to construct an array of hashes
            JSONList arr = (JSONList) map.get(gt.getName());
            if (arr == null) {
                arr = new JSONList();
                map.put(gt.getName(), arr);
            }
            JSONMap rowMap = new JSONMap();
            arr.add(rowMap);
            for (int i = 0; i < size; i++) {
                JSONResponse.setMapEntry(rowMap, classes[i], resp.getShape().getHeaderShort()[i], elems[i]);
            }
        } else {
            for (int i = 0; i < size; i++) {
                int o = offset + i;
                JSONResponse.setMapEntry(map, classes[o], resp.getShape().getHeaderShort()[o], elems[i]);
            }
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
        Class c = classes[index];
        String name = resp.getShape().getHeaderShort()[index];
        JSONResponse.setMapEntry(map, c, name, o);
        return true;
    }

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public void addToResponse(Response response) {
        try {
            map.writeJSONGraph(resp.jsonGenerator);
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        map = new JSONMap();
    }
}
