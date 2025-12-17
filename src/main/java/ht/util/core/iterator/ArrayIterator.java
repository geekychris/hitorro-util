package ht.util.core.iterator;

/**
 *
 */
public class ArrayIterator<E> extends BaseArrayIterator<E> {
    public ArrayIterator(E e[], int maxPos) {
        this.e = e;
        this.maxPos = maxPos;
    }

    public ArrayIterator(E e[]) {
        this.e = e;
        maxPos = e.length;
    }
}
