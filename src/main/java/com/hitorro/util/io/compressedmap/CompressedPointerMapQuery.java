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
package com.hitorro.util.io.compressedmap;


import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.RAMInputStream;

import java.io.IOException;

/**
 *
 */
public class CompressedPointerMapQuery {
    protected TLongIntHashMap longHM;
    protected TIntIntHashMap intHM;
    private CompressedPointerMap cpm;
    private CInputStream postingsFile;
    private boolean useInt;

    public CompressedPointerMapQuery(CompressedPointerMap cpm) {
        this.cpm = cpm;
        this.useInt = cpm.useInt;
        this.longHM = cpm.longHM;
        this.intHM = cpm.intHM;

        RAMInputStream is = new RAMInputStream(null);
        is.setBuffer(cpm.buffer);
        postingsFile = is;
    }

    public long getUpperKeyPart(long key) {
        return (key >> cpm.lowerWordSize);
    }

    /**
     * get the pointer into the slab store, -1 is returned if the key does not exist. First we attack the upper table
     * which gives us a pointer into the postings listFiles to locate the specific key value pair. If we find the upper part
     * of the pointer then we can goto the postings file and enumerate through the entries to find the key.
     * <p/>
     * Missing keys take the longest as we have to enumerate the whole postings listFiles to discover that.
     *
     * @param key
     * @return
     */
    public final long getPtr(long key) throws IOException {
        long k = (key >> cpm.lowerWordSize);
        long priorKey = k << cpm.lowerWordSize;

        long ptr;
        if (useInt) {
            if (!intHM.contains((int) k)) {
                return -1;
            }
            ptr = intHM.get((int) k);
        } else {
            if (!longHM.contains(k)) {
                return -1;
            }
            ptr = longHM.get(k);
        }

        // need more than one instance of the postings file for concurrency
        postingsFile.seek(ptr);
        long priorValue = 0;

        long kP = postingsFile.readVLong();
        while (kP != -1) {
            priorKey = kP + priorKey;
            priorValue = postingsFile.readVLong() + priorValue;
            if (priorKey == key) {
                return priorValue;
            }
            kP = postingsFile.readVLong();
        }
        return -1;
    }
}

