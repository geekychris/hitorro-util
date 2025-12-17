package ht.util.core.opers;

import ht.util.core.GenericKeyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a cascading listFiles of operators that provides the first result object that meets a criteria
 */
public class LogicalSelector<T, E> {
    private List<GenericKeyValue<HTPredicate<T>, E>> list = new ArrayList();
    private HTPredicate<T> t[];
    private E e[];
    private boolean isFinalized = false;

    public void addSelection(HTPredicate<T> oper, E selection) {
        list.add(new GenericKeyValue(oper, selection));
        isFinalized = false;
    }

    public void finishSetup() {
        if (isFinalized) {
            return;
        }
        t = new HTPredicate[list.size()];
        e = (E[]) new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            GenericKeyValue<HTPredicate<T>, E> g = list.get(i);
            t[i] = g.getKey();
            e[i] = g.getValue();
        }
        isFinalized = true;
    }

    public E select(T testMe) {
        for (int i = 0; i < t.length; i++) {
            if (t[i].test(testMe)) {
                return e[i];
            }
        }
        return null;
    }

    public E[] getTargets() {
        return e;
    }
}
