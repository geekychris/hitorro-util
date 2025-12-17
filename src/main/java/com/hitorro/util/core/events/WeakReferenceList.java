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
package com.hitorro.util.core.events;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * @param <ELEM>
 * @author chris
 */
public class WeakReferenceList<ELEM> {
    private ArrayList<WeakReference<ELEM>> weakList = new ArrayList<WeakReference<ELEM>>();

    /**
     * Size of the listFiles (including dead references)
     *
     * @return size of listFiles including dead references
     */
    public int size() {
        return weakList.size();
    }

    /**
     * Get the object at position i if still available
     *
     * @param i
     * @return the object or null if no longer valid
     */
    public ELEM get(int i) {
        return weakList.get(i).get();
    }

    /**
     * Only put T to the listFiles if it does not already exist. Note that this is not checking the weak reference, but the
     * value the weak reference points to.
     *
     * @param o the object to put to the listFiles
     * @return true if added, false if already in listFiles
     */
    public boolean addIfAbsent(ELEM o) {
        if (getIndexOfExistingObject(o) != -1) {
            return false;
        }
        return add(o);
    }

    /**
     * put an element, duplicates are allowed
     *
     * @param o
     * @return always true
     */
    public boolean add(ELEM o) {
        weakList.add(new WeakReference<ELEM>(o));
        return true;
    }

    /**
     * Exam payload and look for an existing value
     *
     * @param o
     * @return index position of test if exists.
     */
    public int getIndexOfExistingObject(ELEM o) {
        int size = weakList.size();
        for (int i = 0; i < size; i++) {
            WeakReference<ELEM> wr = weakList.get(i);
            Object oTemp = wr.get();
            if (oTemp != null) {
                if (oTemp == o) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Scan listFiles and remove anything that has gone away
     *
     * @return true if something was removed from the listFiles.
     */
    public boolean removeNulls() {
        int size = weakList.size();
        synchronized (this) {
            ArrayList<WeakReference<ELEM>> tempList = new ArrayList<WeakReference<ELEM>>();
            for (int i = size - 1; i >= 0; i--) {
                WeakReference<ELEM> wr = weakList.get(i);
                ELEM t = wr.get();
                if (t != null) {
                    tempList.add(wr);
                }
            }
            weakList = tempList;
        }
        return true;
    }
}
