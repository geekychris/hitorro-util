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
package com.hitorro.util.basefile.tools.queue.reader;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.queue.reader.serializer.WalkerDeserializer;
import com.hitorro.util.basefile.tools.queue.reader.serializer.WalkerDeserializerFactory;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.iterator.CloseableIterator;
import com.hitorro.util.typesystem.BaseSession;

import java.io.IOException;
import java.util.Iterator;

/**
 * User: chris
 */
public class StreamingDirectoryVisitorIterator<T> implements Iterator<T> {
    private DirectoryVisitorIterator dwi;
    private CloseableIterator<T> xsi;
    private T curr;
    private BaseFile currentFile = null;
    private boolean shouldNotify = false;
    private boolean initialRead = true;
    private EndFileNotification callback;
    private BaseSession baseSession;
    private WalkerDeserializerFactory factory;
    private Timer timer = new Timer();

    public StreamingDirectoryVisitorIterator(DirectoryVisitorIterator dwi, BaseSession session) {
        this.dwi = dwi;
        this.baseSession = session;
        this.factory = WalkerDeserializerFactory.getInstance();
    }

    public T next() {
        T res;

        if (curr == null) {
            // need to attempt to open the next file
            if (nextFile()) {
                if (!nextAux()) {
                    return null;
                }
            }
        }

        res = curr;
        nextAux();

        return res;
    }

    public void close() throws Exception {
        if (xsi != null) {
            xsi.close();
        }
    }

    public boolean hasNext() {
        initialCase();
        if (curr != null) {
            return true;
        }
        attemptNotify();
        return dwi.hasNext();
    }

    private void attemptNotify() {
        if (shouldNotify) {
            shouldNotify = false;
            if (callback != null) {
                timer.stop();
                callback.endOfBlock(currentFile, timer.getTime());
            }
        }
    }

    private final void initialCase() {
        if (initialRead) {
            nextFile();
            nextAux();
            initialRead = false;
        }
    }

    public void setEndOfCurrentBlock(EndFileNotification callback) {
        this.callback = callback;
        dwi.setEndOfBlockNotificationInterface(callback);
    }

    private boolean nextAux() {
        curr = null;
        if (xsi == null) {
            return false;
        }
        if (xsi.hasNext()) {
            shouldNotify = true;
            curr = xsi.next();
            return true;
        }
        return false;
    }

    private boolean nextFile() {
        do {
            if (!dwi.hasNext()) {
                return false;
            }
            currentFile = dwi.next();
            if (currentFile == null) {
                return false;
            }
        }
        while (!currentFile.exists());

        return moveOnGetNextFile();
    }

    private boolean moveOnGetNextFile() {
        WalkerDeserializer deserializer = factory.getDeserializer(currentFile.getExtension());
        Log.filesystem.info("Deserializing file: %s", currentFile);
        try {
            xsi = deserializer.getIterator(currentFile, baseSession);
            timer.reset();
            timer.start();
        } catch (IOException e) {
            Log.filesystem.error("Unable to get iterator for %s with error %s %e", currentFile, e, e);
            return false;
        }

        return true;
    }

    public void remove() {

    }
}
