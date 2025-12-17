package ht.util.core.diff;

import java.util.Comparator;
import java.util.Iterator;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 19, 2005 Time: 12:16:32 PM
 * <p/>
 * Diff two iterators and determine additions, removals and mods
 */

public class GenericDiffer<E> {
    private E o = null;
    private E c = null;
    private Iterator<E> oT = null;
    private Iterator<E> cT = null;
    private GenericDifferCallback<E> callback = null;

    /**
     * Diff the nodegroup against a listFiles of nodes.  Typically used to determine if a group has gained or lost nodes.
     * Assumes that ID's are in order
     */
    public void diff(Iterator<E> a, Iterator<E> b, Comparator<E> comp, GenericDifferCallback<E> callback) {
        this.callback = callback;
        oT = a;
        cT = b;
        // prime the pump
        advanceOld();
        advanceCurrent();
        while (o != null && c != null) {

            int i = comp.compare(o, c);
            if (i == 0) {
                // same...diffNodeGroupAgainstInstanceList definitions
                callback.call(o, c, Mode.Modify);
                advanceOld();
                advanceCurrent();
            } else if (i < 0) {
                // o is smaller than c its a node that has been removed
                callback.call(o, c, Mode.Remove);
                advanceOld();
            } else {
                callback.call(o, c, Mode.Add);
                advanceCurrent();
            }
        }
        reportAllNodes(o, oT, Mode.Remove, true);
        reportAllNodes(c, cT, Mode.Add, false);
    }

    private void reportAllNodes(E a, Iterator<E> iter, Mode mode, boolean left) {
        while (a != null) {
            if (left) {
                callback.call(a, null, mode);
            } else {
                callback.call(null, a, mode);
            }

            if (iter.hasNext()) {
                a = iter.next();
            } else {
                a = null;
            }
        }
    }

    private boolean advanceOld() {
        if (oT.hasNext()) {
            o = oT.next();
        } else {
            o = null;
        }
        return o != null;
    }

    private boolean advanceCurrent() {
        if (cT.hasNext()) {
            c = cT.next();
        } else {
            c = null;
        }
        return c != null;
    }

    public enum Mode {
        Add, Remove, Modify
    }


}