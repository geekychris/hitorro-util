package ht.util.core.iterator;

import java.util.function.BiFunction;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 6:20:44 PM
 * <p/>
 * Merge two elements together.  Used by the LikeRowMergingIterator to apply rows.
 */
public interface LikeRowMerger<E> extends BiFunction<E, E, E> {
    E apply(E older, E newer);
}
