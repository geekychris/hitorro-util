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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.queue.writer.partitioners.WriterPartitioner;
import com.hitorro.util.basefile.tools.queue.writer.serializer.WriterWritableInterface;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.thread.ThreadTimer;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseSession;

import java.io.IOException;


/**
 * Block writer (writes to a temp file then when mature, oversize or out of partitional range it is moved out to a
 * staging area to push to a queue structure.
 * <p/>
 * Implements also the Sink interface so it can be used in the iterator /put framework
 */
public class BlockFileQueueWriter<T> extends BaseSink<T> {
    public HTPredicate<BaseFile> serFilter;
    BaseFile root;
    private BaseSession session;
    private BaseFile writerRootDirectory;
    private int maximumSecondsBeforeMature;
    private long maximumBytesWritableToFile;
    private ThreadTimer watchdogTimer = null;
    private int objectsWritten = 0;
    private int objectsWrittenTotal = 0;
    private long bytesWritten = 0;
    private int secsWatchDog = 30;
    private int filesWritten = 0;
    private int maxObjects = 0;
    private BaseFile currentFile;
    private int currentSeconds;
    private long lastTouched = -1;
    private DirectoryStepInterface mover;
    private WriterWritableInterface writerWritable;
    private WriterPartitioner partitioner;

    /**
     * Create a writer that cares about size, time and object count.
     *
     * @param session
     * @param writerWritable
     * @param writerDir
     * @param maxSeconds
     * @param maxBytes
     * @param maxObjects
     * @param partitioner
     */
    public BlockFileQueueWriter(BaseSession session,
                                WriterWritableInterface writerWritable,
                                BaseFile writerDir, int maxSeconds, long maxBytes,
                                int maxObjects, WriterPartitioner partitioner,
                                DirectoryStepInterface mover) throws IOException {
        initVars(session, root, writerDir, maxSeconds, maxBytes, maxObjects, writerWritable, partitioner, mover);
        initThreads();
    }

    /**
     * Create writer that only cares about the maximum amount of objects per block.
     *
     * @param session
     * @param writerWritable
     * @param writerDir
     * @param maxObjects
     * @param partitioner
     */
    public BlockFileQueueWriter(BaseSession session,
                                WriterWritableInterface writerWritable,
                                BaseFile writerDir,
                                int maxObjects, WriterPartitioner partitioner,
                                DirectoryStepInterface mover) throws IOException {
        initVars(session, root, writerDir, -1, -1, maxObjects, writerWritable, partitioner, mover);
        initThreads();
    }

    /**
     * **** SINK METHODS *********
     */

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    /*********** END SINK METHODS *************/

    @Override
    public synchronized boolean add(final T sd) throws IOException, StoreException {
        lastTouched = System.currentTimeMillis();
        partitioner.setCurrent(sd);

        int length = writerWritable.getBytes(sd);

        conditionalySwitchFile(length);
        objectsWritten++;
        objectsWrittenTotal++;

        Log.filesystem.debug("Writing count:% total:%s %s out to %s", objectsWritten, objectsWrittenTotal, this.currentFile);
        bytesWritten += length;
        writerWritable.write(sd);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        this.flushAndHoldTillComplete();
        return true;
    }

    private void initThreads() throws IOException {
        sendStaleFilesOut();
        watchdogTimer.start();
        mover.start();
    }


    WriterWritableInterface getWriterWritable() {
        return writerWritable;
    }

    public long getLastTouched() {
        return lastTouched;
    }

    public void resetContent() throws IOException {
        writerRootDirectory.deleteContentOfDir(true);
        partitioner.reset();
        lastTouched = -1;
        currentSeconds = 0;
        bytesWritten = 0;
        objectsWritten = 0;
    }

    public int getTotalObjectsWritten() {
        return objectsWrittenTotal;
    }

    public int getFilesWrittenCount() {
        return filesWritten;
    }

    public synchronized void flushIfMature() throws IOException {
        if (objectsWritten == 0) {
            // do  nothing
            return;
        }
        currentSeconds += secsWatchDog;
        if (currentSeconds >= maximumSecondsBeforeMature) {
            flush();
        }
    }

    public void flushAndHoldTillComplete() throws IOException {
        swapOutFiles();

        mover.notifyMeOfWork();
        currentSeconds = 0;
        objectsWritten = 0;
    }

    public void flush() throws IOException {
        swapOutFiles();
        mover.notifyMeOfWork();
        currentSeconds = 0;
        objectsWritten = 0;
    }

    private void initVars(final BaseSession session,
                          final BaseFile root,
                          final BaseFile writerDir,
                          final int maxSeconds,
                          final long maxBytes,
                          final int maxObjects,
                          final WriterWritableInterface writerWritable,
                          final WriterPartitioner partitioner,
                          final DirectoryStepInterface mover) {
        this.session = session;
        this.root = root;
        this.writerRootDirectory = writerDir;
        this.maximumSecondsBeforeMature = maxSeconds;
        this.maximumBytesWritableToFile = maxBytes;
        this.maxObjects = maxObjects;
        this.writerWritable = writerWritable;
        this.partitioner = partitioner;
        watchdogTimer = new ThreadTimer(new BlockFileQueueWriterWatchdog(this),
                Constants.MillisInSecond * secsWatchDog, true);
        serFilter = new FileEndsWith(writerWritable.getExtension(), true);
        this.mover = mover;
    }

    private void sendStaleFilesOut() throws IOException {

        BaseFile list[] = writerRootDirectory.listFiles(serFilter);
        int count = 0;
        for (BaseFile file : list) {
            if (file.length() == 0) {
                file.delete();
                continue;
            }
            try {
                getWriterWritable().applyCloseToFile(file);
            } catch (IOException e) {
                Log.queue.error("Unable to apply close to file %s %s %e", file, e, e);
            }
            String name = file.getName();
            BaseFile moveToFile = mover.getIncoming().getChild(name);
            moveToFile.mkParentDir();
            file.renameTo(moveToFile);
            count++;
        }
        if (count > 0) {
            filesWritten += count;
            mover.notifyMeOfWork();
        }
    }

    private void conditionalySwitchFile(int length) throws IOException {
        if (partitioner.isWithinFileRange()) {
            // is within the partition boundary
            if (!isOverCapacity(length)) {
                // file still small enough
                if (maximumSecondsBeforeMature == -1 || currentSeconds < maximumSecondsBeforeMature) {
                    // we are either ignoring maturity or we  have not hit the max amount of seconds
                    return;
                }
            }
        }
        swapOutFiles();
    }

    private void swapOutFiles() throws IOException {
        this.writerWritable.close();
        sendStaleFilesOut();
        openFileByDate();
    }

    private boolean openFileByDate() throws IOException {
        if (!partitioner.hasCurrent()) {
            return false;
        }
        String name = Fmt.S("%s-%s.%s", partitioner.getPartitionSequenceNumber(), System.currentTimeMillis(), writerWritable.getExtension());
        currentFile = writerRootDirectory.getChild(name);
        writerRootDirectory.mkdir();
        this.writerWritable.open(currentFile);

        // set start date and end date
        partitioner.setPartitionBoundaries();

        objectsWritten = 0;
        bytesWritten = 0;
        currentSeconds = 0;
        return true;
    }


    private boolean isOverCapacity(int length) {
        if (maximumBytesWritableToFile != -1 && bytesWritten + length > this.maximumBytesWritableToFile) {
            return true;
        }
        return this.objectsWritten > this.maxObjects;
    }
}