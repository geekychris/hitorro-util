package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;
import ht.util.io.largedata.iterator.BaseFileSelectTreeController;

import java.io.IOException;

/**
 *
 */
public class CompressedStreamBaseFileSelectTreeController<T extends CompressedStreamIO> extends BaseFileSelectTreeController {
    protected BaseFileAccessingObjectFactory<T> factory;

    public CompressedStreamBaseFileSelectTreeController(final BaseFile dir, final BaseFile[] fList, final int maxPerIter, final boolean deleteOnceMerged,
                                                        final String finalFileExtension, BaseFileAccessingObjectFactory<T> fac, boolean removeOriginalFiles) throws IOException {
        super(dir, fList, maxPerIter, fac, deleteOnceMerged, finalFileExtension, removeOriginalFiles);
        factory = fac;
    }

    protected AbstractIterator<T> getIterator(BaseFile fc) throws IOException {
        return new CompressedStreamIOIterator(fc, factory);
    }
}
