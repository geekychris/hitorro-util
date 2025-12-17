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
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.ByteUtil;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;

import java.io.IOException;

/**
 * Experimentation on 1.8M slab file to determine lookup rate.  Measurements taken using a laptop on battery power using
 * a hybrid 2.5" disk.
 * <p/>
 * - give a key ordered slab file of 761MB and 1.82M documents created a compressed pointer apply - From the apply we then
 * reiterated the slab pulling each key and ptr offset from that slab.
 * <p/>
 * Cost to read just the slab with no apply lookup = 6 Seconds
 * <p/>
 * Bits Lower Key   |    upper / lower size  |    time  |  lookups ms | second _________________________________________________________________________________________
 * 32               |   348k/11M             |    9     |  3.6K        |  3.65M 34               |   130k/9.6M |    14 |
 * 1.2K        |  1.25M 36               |   33k/9.2M             |    36    |  344         |  345K
 * <p/>
 * keys recorded in upper 177388 average entries per key 10 split size = 32  took=499 retrieves_per_milli=3659
 * retrieves_per_second=3659400.8016032064  upper+file_size=347KB lower_file_size=11MB missing_ptr=0 record_not_found=0
 * <p/>
 * keys recorded in upper 44419 average entries per key 41 split size = 34  took=1466 retrieves_per_milli=1245
 * retrieves_per_second=1245594.1336971351  upper+file_size=129KB lower_file_size=9MB missing_ptr=0 record_not_found=0
 * <p/>
 * keys recorded in upper 11118 average entries per key 164 split size = 36  took=5299 retrieves_per_milli=344
 * retrieves_per_second=344601.05680317036  upper+file_size=32KB lower_file_size=9MB missing_ptr=0 record_not_found=0
 */
public class CompressedPointerMap {
    public static final String UpperKey = "root.ser";

    public static final String LowerKey = "posts.ser";
    public static final String BitsKey = "bits.txt";
    protected int lowerWordSize;
    protected boolean useInt = true;
    protected TLongIntHashMap longHM;
    protected TIntIntHashMap intHM;
    protected byte buffer[];
    private COutputStream upperTable;
    private COutputStream lowerTable;
    private long lowerMask;
    private BaseFile rootFile;
    // XXX assume keys are not negative????
    private long currUpper = 0;
    // ptr to next table starts at 0
    private long utPriorLowerPointer = 0;
    private long priorLowerValue = 0;
    private long priorLowerKey = 0;
    private int keys = 0;
    private long currLowerPtr;
    // For query time
    private boolean hasLoadedForRead = false;

    public CompressedPointerMap(BaseFile rootFileDir, int lowerWordSize) {
        this.rootFile = rootFileDir;
        this.lowerWordSize = lowerWordSize;
        lowerMask = ByteUtil.getLowPartMaskNBits(lowerWordSize);
        rootFile.mkdir();
    }

    public long getUpperTableSize() {
        return rootFile.getChild(UpperKey).length();
    }

    public long getLowerTableSize() {
        return rootFile.getChild(LowerKey).length();
    }

    public synchronized CompressedPointerMapQuery getQuery() throws IOException {
        loadForRead();
        return new CompressedPointerMapQuery(this);
    }

    public int getKeySize() {
        return keys;
    }

    public void add(long key, long value) throws IOException {
        long k = (key >> lowerWordSize);
        if (currUpper != k) {
            keys++;
            // we have changed our upper key, we must store that.  The upper table alread assumes the
            // key is the top n bits of the key.  to save space.  Pointers into the posts file
            // are then represented as delta's.  We could represent this layer in a hash table of
            // compressed key values and decompressed pointers, or this table itself could be
            // some kind of cascade
            upperTable.writeVLong(k - currUpper);

            if (currLowerPtr != 0) {
                // terminate prior posting
                lowerTable.writeVLong(-1);
            }
            // now written we can get its position
            currLowerPtr = lowerTable.getCurrentFileOffset();

            // write the ptr to the lower table in the upper level
            upperTable.writeVLong(currLowerPtr - utPriorLowerPointer);
            utPriorLowerPointer = currLowerPtr;

            // reset the pointer in the lower table
            priorLowerValue = 0;
            currUpper = k;
            priorLowerKey = 0;

        }
        // now lets write the posting.  The keys are stored in delta encoding and so are the values (assuming
        // keys are in order AND values are incremental pointers.
        long lowerPart = key & lowerMask;
        lowerTable.writeVLong(lowerPart - priorLowerKey);
        priorLowerKey = lowerPart;
        lowerTable.writeVLong(value - priorLowerValue);
        priorLowerValue = value;
    }

    public void finish() throws IOException {
        lowerTable.writeVLong(-1);
        lowerTable.close();
        upperTable.writeVLong(Long.MAX_VALUE);
        upperTable.close();
    }

    public boolean openForWrite() throws IOException {
        upperTable = rootFile.getChild(UpperKey).getCOutputStream();
        lowerTable = rootFile.getChild(LowerKey).getCOutputStream();
        return true;
    }

    public boolean loadForRead() throws IOException {
        if (this.hasLoadedForRead) {
            return true;
        }
        hasLoadedForRead = true;
        if (lowerWordSize >= 32) {
            // we are encoding the upper part in something that fits in a int
            intHM = new TIntIntHashMap();
        } else {
            useInt = false;
            longHM = new TLongIntHashMap();
        }
        readInUpperTable();
        buffer = rootFile.getChild(CompressedPointerMap.LowerKey).getBytes();
        return true;
    }

    private void readInUpperTable() throws IOException {
        CInputStream is = rootFile.getChild(UpperKey).getCInputStream();
        long k = is.readVLong();
        long v = 0;
        long prior = 0;
        while (k != Long.MAX_VALUE) {
            prior = prior + k;
            long kV = is.readVLong();
            v = kV + v;
            addRowToHM(prior, v);

            k = is.readVLong();
        }
    }

    private void addRowToHM(long key, long value) {
        if (useInt) {
            intHM.put((int) key, (int) value);
        } else {
            longHM.put(key, (int) value);
        }
    }
}
