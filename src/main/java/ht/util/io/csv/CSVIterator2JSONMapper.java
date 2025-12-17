package ht.util.io.csv;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.JsonValueSource;
import ht.util.core.iterator.mappers.BaseMapper;

/**
 * Wrapper a csviterator in a json iterator
 */
public class CSVIterator2JSONMapper extends BaseMapper<CSVIterator, AbstractIterator<JsonValueSource>> {
    public static final CSVIterator2JSONMapper me = new CSVIterator2JSONMapper();

    @Override
    public AbstractIterator<JsonValueSource> apply(final CSVIterator e) {
        return new CSV2JSONIterator(e);
    }
}
