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
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.thread.RestartableService;
import com.hitorro.util.core.thread.RestartableServiceDaemon;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Copy anything in the outgoing directory to filesystem. Assumes that the directory we are pulling from a file that is
 * of the form:
 * <p/>
 * nnnnnn-mmmmmmmm
 * <p/>
 * Where nnnnnnnnn is actually the date part representing the first object position to write into a specific date
 * location in the queue if you are using a chain, of directory steps this gets a tad more tricky as what is the first
 * date?
 */
public class BaseFileToQueueStructureMover implements Runnable, DirectoryStepInterface {
    public static final String IncomingBoxDir = "incoming";
    private int sleepInSeconds = 60;
    private Object notifier = new Object();
    //private BlockFileQueueWriter writer;
    private boolean compress = false;
    private BaseFile incoming;
    private BaseFile destRoot;
    private BaseFile queueIncoming;
    private boolean goIntoIncoming;

    /**
     * Examine the incoming directory and if there are files move them to the queue.  This queue can either be full
     * queue structure or dump it simply into an outgoing folder.
     *
     * @param compressContent
     * @param incoming
     * @param destRoot
     * @param goIntoIncoming
     */
    public BaseFileToQueueStructureMover(boolean compressContent, BaseFile incoming, BaseFile destRoot, boolean goIntoIncoming) {
        incoming.mkdir();
        this.goIntoIncoming = goIntoIncoming;
        this.destRoot = destRoot;
        queueIncoming = destRoot.getChild(IncomingBoxDir);
        this.compress = compressContent;
        this.incoming = incoming;
    }

    public void start() {
        RestartableService refreshRestarter = new RestartableService("HDFSMover", "HDFSMover", 100, this, true);
        RestartableServiceDaemon.addService(refreshRestarter);
    }

    public Object getNotificationObject() {
        return notifier;
    }

    public void flushTillComplete() {
        try {
            int count = process();
            while (count > 0) {
                count = process();
            }
        } catch (IOException e) {
            Log.queue.error("Unable to process move %s %e", e, e);
        }
    }

    public void run() {
        while (true) {
            try {
                process();
            } catch (IOException e) {
                Log.queue.error("Unable to process move %s %e", e, e);
            }
            Env.sleepNSeconds(sleepInSeconds, notifier);
        }
    }

    private int process() throws IOException {
        // no longer filter by extension
        BaseFile[] files = incoming.listFiles();

        if (!ArrayUtil.nullOrEmpty(files)) {
            Arrays.sort(files);
            // copying files to filesystem.
            for (BaseFile file : files) {
                String name = file.getNameSansExtension();
                String extension = file.getExtension();
                // make sure we have something to
                if (file.length() == 0) {
                    // no need to copy an empty file
                    file.delete();
                    continue;
                }

                if (!StringUtil.nullOrEmptyString(name)) {
                    String parts[] = StringUtil.splitByToken(name, '-');
                    if (parts == null || parts.length != 2) {
                        Log.filesystem.error("Filename should be 2 parts (date-date)");
                        //XXX TO FIX
                        Env.sleepNSeconds(60);
                    } else {
                        BaseFile bf;
                        if (goIntoIncoming) {
                            if (compress) {
                                bf = queueIncoming.getChild(Fmt.S("%s.%s.gz", parts[1], extension));
                            } else {
                                bf = queueIncoming.getChild(Fmt.S("%s.%s", parts[1], extension));
                            }
                        } else {
                            bf = getFileForQueueTarget(parts[0], extension);
                        }

                        try {
                            bf.mkParentDir();
                            if (file.copyTo(bf, compress, true)) {
                                Log.filesystem.debug("Copied %s to %s, deleting local file", file, bf);
                                file.delete();
                            } else {
                                Log.filesystem.error("Unable to copy file %s to %s", file, bf);
                            }
                        } catch (IOException e) {
                            Log.filesystem.error("Exception %s %e", e, e);
                        }
                    }
                }
            }
        }
        return files.length;
    }

    private BaseFile getFileForQueueTarget(final String part, String extension) {
        BaseFile bf;
        long dateTime = Long.parseLong(part);
        Date d = new Date(dateTime);
        String path = DateResolution.LongDay.getFormatted(d);

        if (compress) {
            bf = destRoot.getChild(Fmt.S("%s/%s.%s.gz", path, part, extension));
        } else {
            bf = destRoot.getChild(Fmt.S("%s/%s.%s", path, part, extension));
        }
        return bf;
    }

    @Override
    public void notifyMeOfWork() {
        synchronized (notifier) {
            notifier.notifyAll();
        }
    }

    @Override
    public BaseFile getIncoming() {
        return incoming;
    }
}
