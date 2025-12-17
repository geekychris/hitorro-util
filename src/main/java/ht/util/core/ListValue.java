/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

    
    User: chris
*/

package ht.util.core;

/**
 * Simple class representing a listFiles display string (the label) and the value of the item.
 *
 * @author chris
 */
public class ListValue {
    private String label;
    private Object theValue;

    public ListValue(String lb, Object val) {
        label = lb;
        theValue = val;
    }

    public static void setSingleSelected(ListValue[] vs, Object value, boolean[] sels) {
        int ii;
        for (ii = 0; ii < vs.length; ii++) {
            Object vsval = vs[ii].theValue;
            if ((vsval == null && value == null) || (vsval != null && vsval.equals(value))) {
                sels[ii] = true;
                break;
            }
        }
        if (ii >= vs.length) {
            // didn't find value - let first item be the selection
            sels[0] = true;
        }
    }

    public static void setMultipleSelected(ListValue[] vs, Object[] values, boolean[] sels) {
        if (values == null || vs == null) {
            return;
        }

        int ii;
        for (ii = 0; ii < vs.length; ii++) {
            Object vsval = vs[ii].theValue;
            // is this value selected
            for (Object vv : values) {
                if ((vsval == null && vv == null) || (vsval != null && vsval.equals(vv))) {
                    sels[ii] = true;
                }
            }
        }
    }

    public static ListValue getSelected(ListValue[] vs, boolean[] sels) {
        // find first selected
        if (vs == null || sels == null) {
            return null;
        }
        for (int ii = 0; ii < sels.length; ii++) {
            if (sels[ii]) {
                return vs[ii];
            }
        }

        return null;
    }

    public String getLabel() {
        return label;
    }

    public Object getValue() {
        return theValue;
    }

    /**
     * Interface to be implemented by classes that can provide values for a select listFiles.
     */
    public interface ListValueSource {
        /**
         * Get the select listFiles values for some field on some object.
         *
         * @param obj       - the object instance
         * @param fieldName - the field name
         * @param tag       - a tag, which may be null, to use as a qualifier for the listFiles
         * @return a listFiles of select listFiles values
         */
        ListValue[] getValues(Object obj, String fieldName, String tag);
    }
}
