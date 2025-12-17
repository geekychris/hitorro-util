package ht.util.core.iterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class SortIterator<E> extends AbstractIterator<E> {
    private Iterator<E> iter;
    private Comparator<E> comp;
    private CollectionIterator<E> arrayIter = null;

    public SortIterator(Iterator<E> iter, Comparator<E> comp) {
        this.iter = iter;
        this.comp = comp;
    }

    @Override
    public boolean hasNext() {
        if (arrayIter == null) {
            ArrayList<E> al = new ArrayList();
            while (iter.hasNext()) {
                al.add(iter.next());
            }
            Collections.sort(al, comp);
            arrayIter = new CollectionIterator<E>(al);
        }
        return arrayIter.hasNext();
    }

    @Override
    public E next() {
        return arrayIter.next();
    }
}
