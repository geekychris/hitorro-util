package ht.util.basefile.tools.queue.writer;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BasePivot;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.Fmt;
import ht.util.core.thread.RestartableService;
import ht.util.core.thread.RestartableServiceDaemon;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;

import java.io.IOException;

/**
 *
 */
public abstract class DirectoryStep<E> extends BasePivot implements Runnable, DirectoryStepInterface {
    protected int sleepBetweenPoll = 60;
    protected Object notificationObject = new Object();
    protected boolean inRunMode = false;
    protected DirectoryStepInterface next;
    protected Mapper<BaseFile, AbstractIterator<E>> baseFileToIterator;
    protected BaseFileAccessingObjectFactory<E> factory;
    protected String fileExtensionOut;
    protected boolean started = false;

    public DirectoryStep(Mapper<BaseFile, AbstractIterator<E>> baseFileToIterator,
                         BaseFileAccessingObjectFactory<E> factory,
                         BaseFile root, DirectoryStepInterface next,
                         boolean runInOwnThread, String fileExtensionOut) {
        super(root);
        this.next = next;
        this.factory = factory;
        this.baseFileToIterator = baseFileToIterator;
        inRunMode = runInOwnThread;
        this.fileExtensionOut = fileExtensionOut;
    }


    public void start() {
        if (started) {
            return;
        }
        started = true;
        if (inRunMode) {
            RestartableService rs = new RestartableService(Fmt.S("DirStep-%s", this.getClass().getName()), root.getAbsolutePath(), 100, this, true);
            RestartableServiceDaemon.addService(rs);
        }
        next.start();
    }

    public Object getNotifier() {
        return notificationObject;
    }

    public void notifyMeOfWork() {
        if (!inRunMode) {
            // we are chained together without independent threads.
            try {
                execute();
            } catch (Exception e) {
                Log.queue.error("Unable to execute step executed %s %e", e, e);
            }
            return;
        }

        synchronized (notificationObject) {
            notificationObject.notify();
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                execute();
            } catch (Exception e) {
                Log.queue.error("Unable to execute step executed %s %e", e, e);
            }
            Env.sleepNSeconds(sleepBetweenPoll, notificationObject);
        }
    }

    /**
     * Just move to the next guy
     *
     * @return
     * @throws IOException
     */
    @Override
    public boolean copyFromProcessedOut() throws IOException {
        if (next == null) {
            return false;
        }
        BaseFile targetDir = next.getIncoming();
        targetDir.mkdir();
        BaseFile files[] = this.processedData.listFiles();
        if (files == null) {
            return true;
        }
        for (BaseFile file : files) {
            BaseFile targ = targetDir.getChild(file.getName());
            file.renameTo(targ);
        }
        next.notifyMeOfWork();
        return true;
    }
}
