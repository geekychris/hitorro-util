package ht.util.datefilters;

/**
 * Given parts of a date Y M D and a given cursor depth, determine if a date is within range.
 * <p/>
 * User: chris
 */
public interface DateRangeFilterIntf {
    public int inRange(int[] parts, int depth);
}
