package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.largedata.iterator.BaseFileSelectTreeController;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 31, 2005 Time: 10:55:30 AM
 */
public class CompressedStreamSelectTreeController<T extends CompressedStreamIO> extends BaseFileSelectTreeController {
    protected BaseFileAccessingObjectFactory<T> factory;

    public CompressedStreamSelectTreeController(final BaseFile dir, final BaseFile[] fList, final int maxPerIter, final boolean deleteOnceMerged,
                                                final String finalFileExtension, BaseFileAccessingObjectFactory<T> fac, boolean removeOriginalFiles) throws IOException {
        super(dir, fList, maxPerIter, fac, deleteOnceMerged, finalFileExtension, removeOriginalFiles);
        factory = fac;
    }

    protected Iterator getIterator(File fc) throws IOException {
        return new CompressedStreamIOIterator(fc, factory);
    }
}
