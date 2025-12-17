package ht.util.core.iterator;

import ht.util.core.GenericKeyValue;

import java.util.Iterator;

public class TupleIterator<A, B> extends AbstractIterator<GenericKeyValue<A, B>> {

    private Iterator<A> iterA;
    private Iterator<B> iterB;

    public TupleIterator(Iterator<A> iterA, Iterator<B> iterB) {
        this.iterA = iterA;
        this.iterB = iterB;
    }

    @Override
    public boolean hasNext() {
        boolean res = iterA.hasNext();
        return res | iterB.hasNext();
    }

    @Override
    public GenericKeyValue<A, B> next() {

        A a = iterA.next();
        B b = iterB.next();

        return new GenericKeyValue(a, b);
    }

    @Override
    public void remove() {

    }
}