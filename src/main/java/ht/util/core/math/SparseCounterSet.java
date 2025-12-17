package ht.util.core.math;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.csv.ColumnTableMeta;
import ht.util.io.csv.query.CSVQuery;

/**
 * set of sparse counters addressed by an index position.  Simple convenience mechanism to
 */
public class SparseCounterSet {
    private SparseCounter[] sc;

    private BaseFile bf;
    private boolean sparse;
    private String rootName;

    public SparseCounterSet(BaseFile bf, String rootName, boolean sparse, int size) {
        this.bf = bf;
        this.sparse = sparse;
        this.rootName = rootName;
        sc = new SparseCounter[size];
    }

    public void addFromMeta(ColumnTableMeta meta, String fields[]) {
        // has to be the size of the columns as we are given
        sc = new SparseCounter[meta.getSize()];
        for (String col : fields) {
            add(col, meta.getColumnInt(col));
        }
    }

    public void addFromMeta(CSVQuery meta, String fields[]) {
        // has to be the size of the columns as we are given
        sc = new SparseCounter[fields.length];
        // keep index position of the fields
        for (int i = 0; i < fields.length; i++) {
            add(fields[i], i);
        }
    }

    public void add(String name, int pos) {
        sc[pos] = new SparseCounter(name, "ord", "freq");
    }

    public void increment(int pos, int amount) {
        if (pos >= sc.length) {
            return;
        }
        if (sc[pos] != null) {
            sc[pos].increment(amount);
        }
    }

    public void write() {
        for (int i = 0; i < sc.length; i++) {
            if (sc[i] != null) {
                sc[i].write(bf, rootName, sparse);
            }
        }
    }
}

