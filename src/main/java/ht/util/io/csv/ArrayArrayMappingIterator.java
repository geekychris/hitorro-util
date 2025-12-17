package ht.util.io.csv;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;

import java.io.IOException;
import java.util.Iterator;

/**
 * Map a string array to an object array where the output may not be the same cardinality as the input
 */
public class ArrayArrayMappingIterator extends AbstractIterator<Object[]> {
    private Iterator<String[]> iter;
    private Mapper<String, Object> mappers[];
    private int indices[];

    public ArrayArrayMappingIterator(Iterator<String[]> iter, Mapper<String, Object> mappers[], int indices[]) {
        this.iter = iter;
        this.mappers = mappers;
        this.indices = indices;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Object[] next() {
        String row[] = iter.next();
        Object out[] = new Object[indices.length];
        for (int i = 0; i < indices.length; i++) {
            out[i] = mappers[i].apply(row[indices[i]]);
        }
        return out;
    }

    @Override
    public void remove() {

    }
}
