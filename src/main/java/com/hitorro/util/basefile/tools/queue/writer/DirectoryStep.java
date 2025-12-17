/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.basefile.tools.queue.writer;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BasePivot;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;

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
