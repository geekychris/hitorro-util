package ht.util.json.keys.propaccess;

import java.util.Comparator;

public class PropaccessComp implements Comparator<Propaccess> {
    public static PropaccessComp comp = new PropaccessComp();

    @Override
    public int compare(final Propaccess o1, final Propaccess o2) {
        int size = Math.min(o1.length(), o2.length());
        for (int i = 0; i < size; i++) {
            Part p1 = o1.get(i);
            Part p2 = o2.get(i);
            int v = p1.compare(p2);
            if (v != 0) {
                return v;
            }
        }
        return o1.length() - o2.length();
    }
}
