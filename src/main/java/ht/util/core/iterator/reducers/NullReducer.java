package ht.util.core.iterator.reducers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.BaseReducer;

public class NullReducer<E> implements BaseReducer<E, Integer> {
    public static NullReducer me = new NullReducer();

    public Integer reduce(AbstractIterator<E> iter) {
        int counter = 0;
        while (iter.hasNext()) {
            counter++;
            iter.next();
        }
        return counter;
    }
}
