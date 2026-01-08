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

import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 */
public class GenericDifferContainer<E> implements GenericDifferCallback<E> {
    private List<E> added = new ArrayList<E>();
    private List<E> deleted = new ArrayList<E>();
    private List<E> modified = new ArrayList<E>();

    public void call(final E a, final E b, final GenericDiffer.Mode mode) {
        switch (mode) {
            case Add:
                added.add(b);
                break;
            case Modify:
                modified.add(b);
                break;
            case Remove:
                deleted.add(a);
                break;
        }
    }

    public void clear() {
        added.clear();
        deleted.clear();
        modified.clear();
    }

    public List<E> getAdded() {
        return added;
    }

    public void setAdded(final List<E> added) {
        this.added = added;
    }

    public List<E> getDeleted() {
        return deleted;
    }

    public void setDeleted(final List<E> deleted) {
        this.deleted = deleted;
    }

    public List<E> getModified() {
        return modified;
    }

    public void setModified(final List<E> modified) {
        this.modified = modified;
    }
}
