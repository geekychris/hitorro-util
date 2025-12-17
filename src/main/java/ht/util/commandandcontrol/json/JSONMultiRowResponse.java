package ht.util.commandandcontrol.json;

import ht.util.commandandcontrol.GroupTuple;
import ht.util.commandandcontrol.MultiRowResponse;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseShape;
import ht.util.core.Log;
import ht.util.json.JSONList;
import ht.util.json.JSONMap;

import java.io.IOException;

/**
 *
 */
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
