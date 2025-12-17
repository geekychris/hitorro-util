package ht.util.io.largedata;

import java.io.File;
import java.util.Comparator;

/**
 * User: chris
 * <p/>
 * Assuming file names are of the form <time millis>-<counter>.ext sort them newest to oldest so that a select tree can
 * do its thing on newest to oldest.
 */
public class FileNameComparator implements Comparator<File> {
    private int ascending;

    public FileNameComparator() {
        init(false);
    }

    public FileNameComparator(boolean ascending) {
        init(ascending);
    }

    private void init(final boolean ascending) {
        if (ascending) {
            this.ascending = 1;
        } else {
            this.ascending = -11;
        }
    }

    public int compare(File o1, File o2) {

        return o1.compareTo(o2) * ascending;

    }
}