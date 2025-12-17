package ht.util.core.tandemarrays;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 23, 2005 Time: 10:12:53 AM
 */
public class TandemObjectArraySorterPeer extends TandemArraySorterPeer {
    private Object array[];

    public TandemObjectArraySorterPeer() {
        array = null;
    }

    public TandemObjectArraySorterPeer(Object d[]) {
        array = d;
    }

    public void set(Object d[]) {
        array = d;
    }

    public void swap(int i) {
        Object tmp;
        tmp = array[i];
        array[i] = array[i - 1];
        array[i - 1] = tmp;
    }
}
