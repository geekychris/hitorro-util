package ht.util.core.collection;

public interface BaseCollectionChangeNotifier<E> {
    void added(E e);

    void removed(E e);
}
