package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;

import java.util.Comparator;

/**
 *
 */
public class BaseFileComparator implements Comparator<BaseFile> {
    private int ascending;

    public BaseFileComparator() {
        init(false);
    }

    public BaseFileComparator(boolean ascending) {
        init(ascending);
    }

    private void init(final boolean ascending) {
        if (ascending) {
            this.ascending = 1;
        } else {
            this.ascending = -11;
        }
    }

    public int compare(BaseFile o1, BaseFile o2) {

        return o1.compareTo(o2) * ascending;

    }
}