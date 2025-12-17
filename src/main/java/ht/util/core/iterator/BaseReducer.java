package ht.util.core.iterator;

public interface BaseReducer<E, F> {
    F reduce(AbstractIterator<E> iter);
}

