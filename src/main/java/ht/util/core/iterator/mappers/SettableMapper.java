package ht.util.core.iterator.mappers;

/**
 *
 */
public abstract class SettableMapper<E, I, O> extends BaseMapper<I, O> {
    protected E setElem;

    public void setElem(E e) {
        this.setElem = e;
    }
}
