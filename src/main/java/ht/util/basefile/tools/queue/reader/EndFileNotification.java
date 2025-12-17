package ht.util.basefile.tools.queue.reader;

import ht.util.basefile.fs.BaseFile;

/**
 * Implementor gets called when a file block has finished being read
 * <p/>
 * User: chris
 */
public interface EndFileNotification {
    void endOfBlock(BaseFile baseFile, long timeInMsTaken);

    void endOfBlockPostKeyWrite(BaseFile baseFile);
}
