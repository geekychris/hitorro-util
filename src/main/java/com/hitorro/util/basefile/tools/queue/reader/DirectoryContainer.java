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
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.datefilters.DateRangeFilterIntf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO:
 * Must Sort directory.
 * must be able to recover from LGK
 * must support constraints
 * must support end of block notification
 */
public class DirectoryContainer {
    public static final FileComparator FileNameComparator = new FileComparator();

    private List<DirRow> files = new ArrayList();
    private int current = -1;
    private DirRow row = null;
    private BaseFile dir;
    private HTPredicate<BaseFile> lo;
    private DateRangeFilterIntf drf;
    private int depth;
    private int datePath[];

    private int refreshSeconds;

    public DirectoryContainer(BaseFile dir, HTPredicate<BaseFile> lo, int depth, DateRangeFilterIntf drf, int refreshSeconds, int datePath[]) throws IOException {
        this.dir = dir;
        this.lo = lo;
        this.drf = drf;
        this.depth = depth;
        this.refreshSeconds = refreshSeconds;
        // take the parents and put myself
        if (depth > 0) {
            this.datePath = getNextDateDepth(datePath, dir);
        } else {
            this.datePath = new int[0];
        }

        fetchDir(dir, lo);

    }

    public String toString() {
        return dir.toString();
    }

    public void fetchDir(BaseFile dir, HTPredicate<BaseFile> lo) throws IOException {
        BaseFile bos[] = dir.listFiles(lo);
        Arrays.sort(bos, FileNameComparator);
        files.clear();
        for (BaseFile bo : bos) {
            files.add(new DirRow(bo));
        }
    }

    /**
     * Gives you the next DirRow
     *
     * @return
     * @throws IOException
     */
    public DirRow advanceToKeyPosition(String parts[], int depth) throws IOException {
        int i = this.scanForFileName(parts[depth]);
        if (i == -1) {
            return null;
        }
        if (row != null) {
            // ensure we dont leek
            row.container = null;
        }

        current = i;
        if (current >= files.size()) {
            return null;
        }
        row = files.get(current);
        if (row.isDir) {
            row.container = new DirectoryContainer(row.bf, lo, depth + 1, drf, refreshSeconds, datePath);
            return row.container.advanceToKeyPosition(parts, depth + 1);
        } else {
            // go past that file.  If that isnt valid then thats ok because the key MUST exist, the iterator
            // will fail the hasNext.
            current++;
            if (current >= files.size()) {
                return null;
            }
            row = files.get(current);
            return row;
        }
    }

    /**
     * Gives you the next DirRow
     *
     * @return
     * @throws IOException
     */
    public DirRow advance() throws IOException {
        if (row != null && row.container != null) {
            // lets see if the child can advance
            DirRow dr = row.container.advance();
            if (dr != null) {
                return dr;
            }
        }
        if (current < files.size() - 1) {
            DirRow dr = advanceRowCounter();
            if (dr != null) {
                return dr;
            }
        }

        // nothing more to be found here
        return null;
    }

    DirRow scanForFilesSleep(Object lock) throws IOException {
        DirRow dr = scanForFiles();
        while (dr == null) {
            Env.sleepNSeconds(this.refreshSeconds, lock);
            Log.filesystem.info("Woke from DirectoryContainer sleep.....scanning file system");
            dr = scanForFiles();
        }
        return dr;
    }

    DirRow scanForFiles() throws IOException {
        if (row != null && row.container != null) {
            DirRow dr = row.container.scanForFiles();
            if (dr != null) {
                return dr;
            }
        }

        // consider re-enumerating dir;
        fetchDir(dir, lo);
        // now lets scan for the matching file
        if (row == null && files.size() > 0) {
            // we are new to this directory so lets just return the first position
            current = 0;
            return advanceRow(false);
        }
        int index = scanForFile(row);
        if (index == -1) {
            return null;
        }
        if (index < files.size() - 1) {
            current = index;
            return advanceRowCounter();
        }
        return null;
    }

    private DirRow advanceRowCounter() throws IOException {
        return advanceRow(true);
    }

    private DirRow advanceRow(boolean increment) throws IOException {
        if (row != null) {
            row.container = null;
        }
        while (true) {
            if (increment) {
                current++;
            }

            if (current >= files.size()) {
                return null;
            }
            row = files.get(current);

            int parts[] = getNextDateDepth(this.datePath, row.fileAsInt);
            int comparison = this.drf.inRange(parts, depth + 1);
            if (comparison < 0) {
                // we are below the minimum date
                continue;
            } else if (comparison >= 1) {
                // we are beyond the max date
                return null;
            }

            if (row.isDir) {
                row.container = new DirectoryContainer(row.bf, lo, depth + 1, drf, refreshSeconds, datePath);
                return row.container.advance();
            } else {
                return row;
            }
        }
    }

    private int scanForFile(DirRow row) {

        int l = files.size();
        for (int i = 0; i < l; i++) {
            if (files.get(i).equals(row)) {
                return i;
            }
        }
        return -1;
    }

    private int scanForFileName(String name) {

        int l = files.size();
        for (int i = 0; i < l; i++) {
            if (files.get(i).fileName.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private int[] getNextDateDepth(int da[], BaseFile bf) {
        int nextPart = bf.getNameAsInt();
        return getNextDateDepth(da, nextPart);
    }

    private int[] getNextDateDepth(int da[], int nextPart) {
        int a[] = new int[da.length + 1];
        for (int i = 0; i < da.length; i++) {
            a[i] = da[i];
        }
        a[a.length - 1] = nextPart;
        return a;
    }
}

