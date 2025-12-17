package ht.util.core.iterator.reducers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.BaseReducer;

import java.util.ArrayList;
import java.util.List;

public class ListReducer<E> implements BaseReducer<E, List<E>> {
    public static ListReducer me = new ListReducer();

    public List<E> reduce(AbstractIterator<E> iter) {
        ArrayList<E> list = new ArrayList<>();
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }
}
