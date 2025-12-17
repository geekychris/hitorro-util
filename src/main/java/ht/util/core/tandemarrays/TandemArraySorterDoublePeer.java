package ht.util.core.tandemarrays;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class TandemArraySorterDoublePeer extends TandemArraySorterPeer {
    private double m_d[];

    public TandemArraySorterDoublePeer() {
        m_d = null;
    }

    public TandemArraySorterDoublePeer(double d[]) {
        m_d = d;
    }

    public void set(double d[]) {
        m_d = d;
    }

    public void swap(int i) {
        double tmp;
        tmp = m_d[i];
        m_d[i] = m_d[i - 1];
        m_d[i - 1] = tmp;
    }

}
