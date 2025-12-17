package ht.util.io.largedata;

import ht.util.core.iterator.LikeRowMerger;

/**
 * Always return the right one, assuming that the right one is always the newer channel.
 * <p/>
 * User: chris
 */
public class TakeRightRowMerger<T> implements LikeRowMerger<T> {
    public static final TakeRightRowMerger me = new TakeRightRowMerger();

    public T apply(final T old, final T newval) {
        return newval;
    }
}
