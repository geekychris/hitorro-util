package ht.util.core.tandemarrays;

/**
 *
 */
public class TandemArraySorterIntPeer extends TandemArraySorterPeer {
    private int m_d[];

    public TandemArraySorterIntPeer() {
        m_d = null;
    }

    public TandemArraySorterIntPeer(int d[]) {
        m_d = d;
    }

    public void set(int d[]) {
        m_d = d;
    }

    public final void swap(int i) {
        int tmp;
        tmp = m_d[i];
        m_d[i] = m_d[i - 1];
        m_d[i - 1] = tmp;
    }

}