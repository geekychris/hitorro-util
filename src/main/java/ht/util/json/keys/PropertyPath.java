package ht.util.json.keys;

import ht.util.core.string.StringUtil;


/**
 * Created by chris on 7/16/17.
 */
public class PropertyPath {
    private static int initialPathLength = 8;
    private String pathNames[] = new String[initialPathLength];
    private String pathValues[] = new String[initialPathLength];
    private int pathLength = 0;

    public PropertyPath(String path) {
        push(path);
    }

    public int size() {
        return pathLength - 1;
    }

    public boolean isIndexed(int index) {
        if (pathLength >= 0) {
            return pathValues[index] != null;
        }
        return false;
    }

    public void push(String path) {
        int prev = 0;
        int index = path.indexOf('.');
        while (index > 0) {
            String sub = path.substring(prev, index);

            pushSingleStage(sub);
            prev = index + 1;
            index = path.indexOf('.', index + 1);
        }
    }

    public String getIndexValue() {
        return pathValues[pathLength - 1];
    }

    public void setIndexValue(String value) {
        if (pathLength >= 0) {
            pathValues[pathLength - 1] = value;
        }
    }

    public String getIndexValue(int index) {
        if (index >= 0) {
            return pathValues[index];
        }
        return null;
    }

    public String getPathName(int index) {
        if (index >= 0) {
            return pathNames[index];
        }
        return null;
    }

    public String getLastPathName() {
        return getPathName(pathLength - 1);
    }

    public String pop() {
        if (pathLength == 0) {
            return null;
        }

        String v = getLastPathName();
        pathLength--;
        return v;
    }

    public int getIndex() {
        return getIndex(pathLength - 1);
    }

    public void setIndex(int index) {
        setIndexValue(Integer.toString(index));
    }

    public int getIndex(int index) {
        String v = getIndexValue(index);
        if (v == null) {
            return -1;
        }
        return Integer.getInteger(v);
    }

    private void pushSingleStage(String sub) {
        int index = sub.indexOf('[');
        if (index == -1) {
            ensureCapacity();
            pathNames[pathLength] = sub;
            pathValues = null;
            pathLength++;
        }

    }

    private void ensureCapacity() {
        if (pathNames.length == pathLength) {
            pathNames = StringUtil.increaseStringArray(pathNames, pathLength);
            pathValues = StringUtil.increaseStringArray(pathValues, pathLength);
        }
    }
}
