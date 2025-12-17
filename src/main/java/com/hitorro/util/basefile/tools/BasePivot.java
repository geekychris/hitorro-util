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
package com.hitorro.util.basefile.tools;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.ArrayUtil;

import java.io.IOException;

/**
 * Mechanism to attempt to process data in an incoming directory. - Data is taken from there in chunks and moved to an
 * "inProcess" directory. - InProcessed data is then processed by whatever means into the "processedData" - Once the
 * processing is done we mark that we have entered the "processed" phase. - From processed phase we can now handle the
 * "processed", such as copy it to the next pivot or out of the chain of processing - Once all "processed" data is
 * processed we can remove the state information and look back at the incoming directory again to try processing all
 * over again.
 * <p/>
 * recovery: The purpose of the pivot is to allow recovery of whatever we were doing if we restart.  We may of simply
 * been copying data, had data in the processing state or not doing anything at all.  For this reason we have to work
 * somewhat in reverse:
 * <p/>
 * "processed" : we must apply the copy phase
 */
public abstract class BasePivot {
    protected BaseFile root;
    protected BaseFile incoming;
    protected BaseFile inProcess;
    protected BaseFile processedData;
    protected BaseFile state;

    public BasePivot(BaseFile root) {
        this.root = root;
        state = root.getChild("state.txt");

        incoming = root.getChild("incoming");
        incoming.mkdir();
        inProcess = root.getChild("inprocess");
        inProcess.mkdir();
        processedData = root.getChild("processeddata");
        processedData.mkdir();
    }

    public BaseFile getIncoming() {
        return incoming;
    }

    /**
     * see if we were done processing, if we were then we need to complete copy and cleanup. If not see if we had
     * anything in the inProcess directory, clean out half finished stuff in the processed data and execute the process
     * again. If all that is done, copy some more stuff from the incoming directory into the in process and start all
     * over again.
     *
     * @return
     * @throws IOException
     */
    public boolean execute() throws Exception {
        if (done()) {
            // complete copying of data from processeddata directory
            copyCleanup();
        }
        // first cleanup any residue
        if (attemptProcessProcessingDir()) {
            return false;
        }
        // now lets get some more punishment!
        moveFromIncomingToProcessing();
        return !attemptProcessProcessingDir();
    }

    private boolean attemptProcessProcessingDir() throws Exception {
        if (inProcessContainsWork()) {
            // we should process what is in the incoming directory
            cleanupProcessedData();
            if (process()) {
                // we processed lets say we have processed while we copy cleanup.
                writeState();
                copyCleanup();
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Default detection that there is work to be done in the inProcess directory
     *
     * @return
     * @throws IOException
     */
    public boolean inProcessContainsWork() throws IOException {
        BaseFile files[] = inProcess.listFiles();
        return !ArrayUtil.nullOrEmpty(files);
    }

    /**
     * Take whatever is in the inprocess directory and work on it, placing output in the processed directory.
     *
     * @return
     */
    public abstract boolean process() throws Exception;

    /**
     * data is fully processed and in the processedData directory.  We must now ensure its fully "copied" or disposed of
     * by whatever means.
     *
     * @return
     */
    public abstract boolean copyFromProcessedOut() throws IOException;


    /**
     * Default implementation, just move anything in the incoming into the processing staging area.
     *
     * @return
     */
    public boolean moveFromIncomingToProcessing() throws IOException {
        BaseFile files[] = incoming.listFiles();
        if (files == null) {
            return true;
        }
        inProcess.mkdir();
        for (BaseFile file : files) {
            BaseFile dest = inProcess.getChild(file.getName());
            file.renameTo(dest);
        }
        return true;
    }

    public boolean cleanupProcessedData() throws IOException {
        return processedData.deleteContentOfDir(true);
    }

    public boolean cleanupInProcessData() throws IOException {
        return inProcess.deleteContentOfDir(true);
    }


    private void writeState() throws IOException {
        state.writeString("doneprocessing");
    }

    private void removeState() throws IOException {
        state.delete();
    }

    private boolean done() {
        return state.exists();
    }

    private void copyCleanup() throws IOException {
        copyFromProcessedOut();
        cleanupProcessedData();
        cleanupInProcessData();
        removeState();
    }
}
