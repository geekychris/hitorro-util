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
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.datefilters.DateRangeFilterIntf;
import com.hitorro.util.datefilters.NoOpFilter;
import com.hitorro.util.io.FileUtil;

import java.io.IOException;

/**
 * Walks over a directory structure of files.   This can run in two modes - non sleep mode.  Once we get to the end of
 * the files we are done. - sleep mode. Once we get to the end of the files that exist, wait for more.
 * <p/>
 * Files must be created in incremental integer values.  directories can be arbitrary depth, but one format would be:
 * <p/>
 * root/yyyyy/mm/dd/ssssssssss.ext
 * <p/>
 * Nothing stops you from using a deeper or a shallower tree.
 * <p/>
 * User: chris
 */
public class DirectoryVisitorIterator extends AbstractIterator<BaseFile> {
    DirectoryContainer dcRoot = null;
    private BaseFile root;
    private BaseFile currentFile;
    private int refreshSeconds = -1;
    private GoodFileKeyWriter lgk;
    private String priorKey = null;
    private EndFileNotification eobni;
    private Object lock = new Object();

    public DirectoryVisitorIterator(BaseFile root, int refreshSeconds, GoodFileKeyWriter lgk,
                                    HTPredicate<BaseFile> fileFilter) throws IOException {
        this(root, refreshSeconds, lgk, fileFilter, null);
    }

    /**
     * Constructor that doesnt need a good key writer and runs through only once
     *
     * @param root
     * @param fileFilter
     * @throws java.io.IOException
     */
    public DirectoryVisitorIterator(BaseFile root,
                                    HTPredicate<BaseFile> fileFilter) throws IOException {
        this(root, -1, null, fileFilter, null);
    }

    /**
     * Constructor that doesnt need a good key writer and runs through only once
     *
     * @param root
     * @param fileFilter
     * @throws java.io.IOException
     */
    public DirectoryVisitorIterator(BaseFile root,
                                    HTPredicate<BaseFile> fileFilter,
                                    DateRangeFilterIntf dateRangeFilter) throws IOException {
        this(root, -1, null, fileFilter, dateRangeFilter);
    }

    public DirectoryVisitorIterator(BaseFile root, int refreshSeconds, GoodFileKeyWriter lgk,
                                    HTPredicate<BaseFile> fileFilter, DateRangeFilterIntf dateRangeFilter) throws IOException {
        init(root, refreshSeconds, lgk, fileFilter, dateRangeFilter);
    }

    public String toString() {
        if (currentFile == null) {
            return "DVI has no file";
        }
        return currentFile.toString();
    }

    public void setEndOfBlockNotificationInterface(EndFileNotification eobni) {
        this.eobni = eobni;
    }

    public BaseFile next() {
        BaseFile f = currentFile;
        Log.filesystem.debug("Advancing to %s", f);
        writeKey();
        currentFile = null;
        return f;
    }

    public void remove() {
        // Not needed
    }

    private void init(BaseFile root, int refreshSeconds, GoodFileKeyWriter lgk,
                      HTPredicate<BaseFile> fileFilter, DateRangeFilterIntf dr)
            throws IOException {
        if (lgk == null) {
            lgk = new GoodFileKeyWriter(FileUtil.getUniqueTmpFile("lgk"));
        }
        if (dr == null) {
            dr = new NoOpFilter();
        }
        this.root = root;
        this.refreshSeconds = refreshSeconds;
        this.lgk = lgk;
        String key = lgk.getGoodKey();

        dcRoot = new DirectoryContainer(root, fileFilter, 0, dr, refreshSeconds, new int[0]);
        String parts[] = lgk.getParts();
        DirRow d;
        if (ArrayUtil.nullOrEmpty(parts)) {
            // meaningless LGK
            Log.filesystem.info("Last Good Key was not set looking in: %s", lgk.getSource());
            d = dcRoot.advance();
        } else {
            d = dcRoot.advanceToKeyPosition(parts, 0);
        }

        if (d != null) {
            currentFile = d.bf;
        }
    }

    public boolean hasNext() {
        if (currentFile != null) {
            return true;
        }
        DirRow d = null;
        try {
            d = dcRoot.advance();
        } catch (IOException e) {
            Log.filesystem.error("Unable to refresh the content tree %s %e", e, e);
        }
        if (d != null) {
            currentFile = d.bf;
            return true;
        }
        if (refreshSeconds != -1) {
            return nextValueWithSleep();
        }
        return false;
    }

    private boolean nextValueWithSleep() {
        try {
            DirRow dc = dcRoot.scanForFilesSleep(lock);
            if (dc != null) {
                currentFile = dc.bf;
            }
            if (currentFile == null) {
                writeKey();
            }
        } catch (IOException e) {
            Log.filesystem.error("Unable to refresh the dir tree %s %e", e, e);
            return false;
        }

        Log.filesystem.info("directory container got: %s", currentFile);

        return currentFile != null;
    }

    private void writeKey() {
        String k = null;
        if (currentFile != null) {
            k = currentFile.getAbsolutePath();
            k = k.substring(root.getAbsolutePath().length());

        }
        if (priorKey != null) {
            lgk.save(priorKey);
            attemptNotify();

        }
        priorKey = k;
    }

    private void attemptNotify() {
        if (eobni != null) {
            eobni.endOfBlockPostKeyWrite(currentFile);
        }
    }

    @Override
    public void close() throws Exception {
    }
}

