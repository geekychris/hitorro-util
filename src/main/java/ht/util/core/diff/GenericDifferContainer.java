package ht.util.core.diff;

import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 19, 2005 Time: 12:44:52 PM
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
