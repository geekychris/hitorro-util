package ht.util.basefile.tools.queue.writer;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;
import ht.util.io.largedata.iterator.BaseFileSelectTreeController;

/**
 *
 */
public class MergerDirectoryStep<E> extends DirectoryStep<E> {

    public MergerDirectoryStep(String fileExtensionOut,
                               Mapper<BaseFile, AbstractIterator<E>> baseFileToIterator,
                               BaseFileAccessingObjectFactory<E> factory,
                               BaseFile root, DirectoryStepInterface next,
                               boolean runInOwnThread) {
        super(baseFileToIterator, factory, root, next, runInOwnThread, fileExtensionOut);
    }

    @Override
    public boolean process() throws Exception {
        BaseFile tmpDir = root.getChild("tmp");
        tmpDir.mkdir();
        BaseFile files[] = inProcess.listFiles();
        BaseFileSelectTreeController controller = new BaseFileSelectTreeController(tmpDir, files, 5, factory, false, fileExtensionOut, true);
        BaseFile merged = controller.merge();
        if (merged != null && merged.exists()) {
            BaseFile target = this.processedData.getChild(merged.getName());
            merged.renameTo(target);
        }
        return true;
    }
}