package ht.util.basefile.tools.queue.writer;

import ht.util.basefile.fs.BaseFile;

/**
 *
 */
public interface DirectoryStepInterface {
    void notifyMeOfWork();

    BaseFile getIncoming();

    void start();
}
