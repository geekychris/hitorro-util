package ht.util.core;

import java.util.Comparator;

/**
 * Allow comparing of an object that implements ColumnAccessor by a column of our choice
 */
public class ColumnComparator<T> implements Comparator<ColumnAccessor<T>> {
    private Comparator<T> comp;
    private int col;

    public ColumnComparator(Comparator<T> comp, int col) {
        this.comp = comp;
        this.col = col;
    }


    @Override
    public int compare(final ColumnAccessor<T> t1, final ColumnAccessor<T> t2) {
        T t1C = t1.getElement(col);
        T t2C = t2.getElement(col);
        return comp.compare(t1C, t2C);
    }
}
