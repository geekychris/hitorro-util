package ht.util.core.iterator;

import java.util.Iterator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 4:52:59 PM
 */
public interface CloseableIterator<T> extends Iterator<T>, AutoCloseable {
}
