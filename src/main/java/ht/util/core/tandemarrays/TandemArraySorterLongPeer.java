package ht.util.core.tandemarrays;

/**
 *
 */
public class TandemArraySorterLongPeer extends TandemArraySorterPeer {
    private long m_d[];

    public TandemArraySorterLongPeer() {
        m_d = null;
    }

    public TandemArraySorterLongPeer(long d[]) {
        m_d = d;
    }

    public void set(long d[]) {
        m_d = d;
    }

    public void swap(int i) {
        long tmp;
        tmp = m_d[i];
        m_d[i] = m_d[i - 1];
        m_d[i - 1] = tmp;
    }

}

