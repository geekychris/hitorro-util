package ht.util.core;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 19, 2004 Time: 9:38:48 AM
 */
public interface PooledStorageElement {
    int getRealStorageSize();

    void activate();

    void pasivate();
}
