package ht.util.core.iterator;

import ht.util.core.GenericKeyValue;

public interface FillBufferHandler<I, O> {
    void fill(GenericKeyValue<I, O> arr[], int currentSize) throws Exception;
}
