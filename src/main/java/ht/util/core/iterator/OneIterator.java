package ht.util.core.iterator;

public class OneIterator<E> extends AbstractIterator<E> {
    private E elem;

    public OneIterator(E elem) {
        this.elem = elem;
    }

    @Override
    public boolean hasNext() {
        return elem != null;
    }

    @Override
    public E next() {

        E ret = elem;
        elem = null;
        return ret;
    }

    @Override
    public void remove() {

    }
}

