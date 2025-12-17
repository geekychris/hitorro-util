package ht.util.io.csv;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;


/**
 *
 */
public class CSVIteratorImpl extends AbstractIterator<String[]> implements CSVIterator {
    private String[] line;
    private ColumnTableMeta meta = null;

    private CSVReaderBase rb;

    public CSVIteratorImpl(BaseFile filepath, String encoding) throws IOException {
        rb = new CSVReaderBase(filepath, encoding);
        readNextLineAux();
    }

    public CSVIteratorImpl(BaseFile filepath, char encoding) throws IOException {
        rb = new CSVReaderBase(filepath, "UTF-8", encoding);
        readNextLineAux();
    }

    public CSVIteratorImpl(File filepath, char seperator) throws FileNotFoundException {
        rb = new CSVReaderBase(filepath, seperator);
        readNextLineAux();
    }

    public CSVIteratorImpl(Reader rdr, char seperator) {
        rb = new CSVReaderBase(rdr, seperator);
        readNextLineAux();
    }

    public ColumnTableMeta getMeta() {
        if (meta == null) {
            meta = ColumnTableMeta.init(getColumnNames());
        }
        return meta;
    }

    public void enableColumnFixup(boolean flag) {
        rb.adjustColumns = flag;
    }

    private void readNextLineAux() {
        try {
            line = null;
            line = rb.getNextRow();
        } catch (IOException e) {
            Log.io.error("Unable to read from csv  %s %e", e, e);
        }
    }

    @Override
    public boolean hasNext() {
        return line != null;
    }

    @Override
    public String[] next() {
        String ret[] = line;
        readNextLineAux();
        return ret;
    }

    @Override
    public void remove() {
    }

    @Override
    public String[] getColumnNames() {
        return rb.getColumnNames();
    }

    @Override
    public void close() throws Exception {
        rb.close();
    }
}
