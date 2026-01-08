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
package com.hitorro.util.core.diff;

import java.util.Comparator;
import java.util.Iterator;

/**
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