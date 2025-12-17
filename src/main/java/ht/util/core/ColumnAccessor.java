package ht.util.core;

/**
 * Access columns of data from an object that may not typically be represented as a simple array or listFiles
 */
public interface ColumnAccessor<T> {
    public T getElement(int i);

    public int getColumnCount();
}
