package ht.util.core;

import java.util.Vector;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 9:37:22 AM
 * <p/>
 * Description:
 */
public class SettableVector extends Vector {
    /**
     * Set a value, extending vector if necessary
     *
     * @param i
     * @param obj
     * @return obj
     */
    public Object set(int i, Object obj) {
        if (i >= size()) {
            setSize(i + 1);
        }
        return super.set(i, obj);
    }
}

