package ht.util.datefilters;

/**
 * User: chris
 */
public class NoOpFilter implements DateRangeFilterIntf {
    public int inRange(int[] parts, int depth) {
        return 0;
    }
}
