package ht.util.core;

import java.util.Comparator;

/**
 * Comparator that uses an array of objects that represent primitives like long, int, string.
 * Comparisons are done in a cascading way from left to right.  Comparisons can be ascending or descending.
 */
public class JavaObjectOrderFrameComparator implements Comparator<JavaObjectOrderEnumCompInterface> {
    private JavaObjectOrderEnum barrel[];
    private String names[];
    private boolean initialized = false;

    @Override
    public int compare(final JavaObjectOrderEnumCompInterface left,
                       final JavaObjectOrderEnumCompInterface right) {
        if (!initialized) {
            // use one of the objects to setup the comparison frame
            barrel = left.getBarrelSorter();
            names = JavaObjectOrderEnum.getNames(barrel);
        }
        return JavaObjectOrderEnum.compare(left, right, barrel, names);
    }
}
