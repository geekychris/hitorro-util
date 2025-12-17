package ht.util.core.diff;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 19, 2005 Time: 12:27:28 PM
 */
public interface GenericDifferCallback<E> {
    void call(E a, E b, GenericDiffer.Mode mode);
}
