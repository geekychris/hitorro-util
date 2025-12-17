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
package com.hitorro.util.io.filedirwatch;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.EventListener;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.filefilters.FilenameExtensionFilter;
import com.hitorro.util.io.filefilters.IsDirectoryFilenameFilter;
import com.hitorro.util.io.filefilters.OrCollection;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Watch a directory for either too many files or too much space usage. On that condition we must perform an action on
 * the oldest files.  This action could be to simply delete the files, or compress, or something!
 *
 * @author chris
 */
public class DirectoryWatch implements EventListener {
    private File dir;
    private String ext;
    private FilenameFilter filter;
    private int maxFiles;
    private long maxBytes;
    private DirWatcherTask executeme;
    private DirectoryWatch m_chain;
    private boolean recurseDirectoryStructure = false;
    private CompareFileLastModified fileComparator = new CompareFileLastModified();
    private IsDirectoryFilenameFilter directoryFilter = new IsDirectoryFilenameFilter();
    private OrCollection orFilter = new OrCollection();

    /**
     * @param dir
     * @param extension
     * @param maxFiles
     * @param maxBytes
     * @param action
     * @param chain
     * @param recurse
     */
    public DirectoryWatch(File dir,
                          String extension,
                          int maxFiles,
                          long maxBytes,
                          DirWatcherTask action,
                          DirectoryWatch chain,
                          boolean recurse) {
        ext = extension;
        this.dir = dir;
        recurseDirectoryStructure = recurse;
        filter = new FilenameExtensionFilter(extension, true);

        if (recurseDirectoryStructure) {
            orFilter.addFilter(filter);
            orFilter.addFilter(directoryFilter);
        }

        this.maxFiles = maxFiles;
        this.maxBytes = maxBytes;
        if (action != null) {
            executeme = action;
        } else {
            executeme = new DeleteTask();
        }
        m_chain = chain;
    }

    public void check() {
        boolean sort = false;
        File[] list = getFiles();
        int overSize = 0;
        if (list != null && list.length > 0) {
            overSize = list.length - maxFiles;
            if (overSize > 0) {
                sort(list);
                sort = true;
                for (int i = 0; i < overSize; i++) {
                    try {
                        if (executeme.execute(list[i])) {
                            list[i] = null;
                        }
                    } catch (IOException e) {
                        Log.util.error("Exception %s %e", e, e);
                    }
                }
            }
            // remove entries till we get below the size limit
            long totalSize = getTotalSize(list);
            long overLimit = totalSize - maxBytes;
            if (overLimit > 0) {
                if (!sort) {
                    sort(list);
                }
                for (int i = 0; i < list.length; i++) {
                    if (overLimit <= 0) {
                        return;
                    }
                    if (list[i] != null) {
                        try {
                            long tempSize = sizeRounded(list[i]);
                            executeme.execute(list[i]);
                            overLimit -= tempSize;
                            list[i] = null;
                        } catch (IOException e) {
                            Log.util.error("Exception %s %e", e, e);
                        }

                    }
                }
            }
        }
        // if we have another watcher in the chain then call it.
        if (m_chain != null) {
            m_chain.check();
        }
    }

    private File[] getFiles() {
        if (recurseDirectoryStructure) {
            List<File> list = new ArrayList<File>();
            getFilesRecursively(list, this.dir);
            File[] consumeMe = new File[list.size()];
            list.toArray(consumeMe);
            return consumeMe;
        }
        return dir.listFiles(filter);
    }

    private void getFilesRecursively(List<File> list, File dir) {
        File[] l = dir.listFiles(orFilter);
        for (File f : l) {
            if (f.isDirectory()) {
                getFilesRecursively(list, f);
            } else {
                list.add(f);
            }
        }
    }

    private long getTotalSize(File list[]) {
        long total = 0;
        for (File f : list) {
            if (f != null) {
                total += f.length();
            }
        }
        return total;
    }

    private long sizeRounded(File f) {
        if (f != null) {
            long size = f.length();
            return FileUtil.roundLengthToBlockSize(size);
        }
        return 0;
    }

    private void sort(File list[]) {
        Arrays.sort(list, fileComparator);
    }

    public boolean event(String topic, String subTopic, Object args) {
        check();
        return true;
    }

    public String eventName() {
        return Fmt.S("Directory Watcher ext: %s, path: %s task: %s",
                this.dir.getAbsolutePath(),
                this.ext,
                this.executeme.getClass().getCanonicalName());
    }

    /**
     * Directory watching is done in its own thread.
     *
     * @return
     */
    public boolean runAsync() {
        return true;
    }
}


