package ht.util.basefile.tools.queue.writer;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;
import ht.util.io.largedata.buckets.BaseFileBucketWriter;

import java.io.IOException;

/**
 * Take an input directory and sort it, placing ordered output into the output location.
 */
public class SorterDirectoryStep<E> extends DirectoryStep<E> {
    protected BaseFileBucketWriter bucketWriter;

    public SorterDirectoryStep(int maxEntries, String fileExtensionOut,
                               Mapper<BaseFile, AbstractIterator<E>> baseFileToIterator,
                               BaseFileAccessingObjectFactory<E> factory,
                               BaseFile root, DirectoryStepInterface next, boolean runInOwnThread) {
        super(baseFileToIterator, factory, root, next, runInOwnThread, fileExtensionOut);
        bucketWriter = new BaseFileBucketWriter(maxEntries, this.processedData, fileExtensionOut, factory);
    }

    @Override
    public boolean process() throws IOException {
        synchronized (bucketWriter) {
            BaseFile files[] = this.inProcess.listFiles();
            for (BaseFile bf : files) {
                baseFileToIterator.apply(bf).sink(bucketWriter);
            }
            return true;
        }
    }
}
