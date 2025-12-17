package ht.util.core.iterator.reducers;

import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.BaseReducer;

import java.util.Comparator;

public class TupleComparatorReducer<E> implements BaseReducer<GenericKeyValue<E, E>, GenericKeyValue<Integer, Integer>> {

    private Comparator<E> comp;

    public TupleComparatorReducer(Comparator<E> comp) {
        this.comp = comp;
    }

    @Override
    public GenericKeyValue<Integer, Integer> reduce(AbstractIterator<GenericKeyValue<E, E>> iter) {
        int eq = 0;
        int neq = 0;
        while (iter.hasNext()) {
            GenericKeyValue<E, E> elem = iter.next();
            if (comp.compare(elem.getKey(), elem.getValue()) == 0) {
                eq++;
            } else {
                neq++;
            }
        }
        return new GenericKeyValue(eq, neq);
    }
}
