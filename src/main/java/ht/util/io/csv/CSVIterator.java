package ht.util.io.csv;

import ht.util.core.iterator.ChainingIteratorIntf;

/**
 *
 */
public interface CSVIterator extends ChainingIteratorIntf<String[]> {
    String[] getColumnNames();
}
