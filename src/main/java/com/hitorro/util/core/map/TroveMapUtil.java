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
package com.hitorro.util.core.map;

import gnu.trove.iterator.TLongIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.hash.*;
import gnu.trove.set.hash.TLongHashSet;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.io.FileUtil;

import java.io.*;
import java.util.Iterator;

/**
 * Created by chris on 12/31/16.
 */
public class TroveMapUtil {
    /**
     * apply one maps counts or existence to another apply.  runs in two modes: "sum"   mode will take the long value
     * from the source apply entry and sum it with the target. "!sum" increments by 1 in the target apply for each entry
     * found in the source apply.
     *
     * @param from
     * @param to
     * @param sum
     * @return
     */
    public static final int applyMap(TObjectLongHashMap from, TObjectLongHashMap to, boolean sum) {
        TObjectLongIterator iter = from.iterator();
        Object key;
        long val;
        int count = 0;
        if (sum) {
            for (TObjectLongIterator it = from.iterator(); it.hasNext(); ) {
                it.advance();
                val = it.value();
                key = it.key();
                if (to.contains(key)) {
                    to.adjustValue(key, to.get(key) + val);
                } else {
                    to.put(key, val);
                }
                count++;
            }
        } else {
            for (TObjectLongIterator it = from.iterator(); it.hasNext(); ) {
                it.advance();
                val = it.value();
                key = it.key();
                if (to.contains(key)) {
                    to.increment(key);
                } else {
                    to.put(key, 1);
                }
                count++;
            }
        }
        return count;
    }

    /**
     * read a long set into a hashset using the file size as in indicator on how many rows to fetch.
     *
     * @param f
     * @return
     * @throws IOException
     */
    public static final TLongHashSet getTLongHashSetFromFile(File f, int initialSize) throws IOException {

        if (f.exists()) {
            int size = (int) (f.length() / 8);
            DataInputStream dis = FileUtil.getDataInputStreamForFile(f);
            TLongHashSet set = new TLongHashSet(Math.max(size, initialSize));
            for (int i = 0; i < size; i++) {
                set.add(dis.readLong());
            }
            return set;
        }
        return null;
    }

    /**
     * Write the contents of the set to a file.  If the file doesnt exist it will be created.  If it does exist and we
     * are in append mode we will append to that file, else we will exit without writing.
     *
     * @param file
     * @param set
     * @param append
     * @return
     * @throws IOException
     */
    public static final boolean writeLongSetToFile(File file, TLongHashSet set, boolean append) throws IOException {
        DataOutputStream dos;
        if (file.exists()) {
            if (append) {
                dos = FileUtil.getDataOutputStreamForFile(file, true);
            } else {
                return false;
            }
        } else {
            FileUtil.ensureParentDirectories(file, true);
            dos = FileUtil.getDataOutputStreamForFile(file);
        }
        TLongIterator iter = set.iterator();
        while (iter.hasNext()) {
            long l = iter.next();
            dos.writeLong(l);
        }
        dos.flush();
        dos.close();
        return true;
    }

    /**
     * Write a LongInt hashmap to a file, along with the size entries.
     *
     * @param f
     * @param map
     * @return
     * @throws IOException
     */
    public static final boolean writeTLongLongMapToFile(File f, TLongLongHashMap map) throws IOException {
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        return writeTLongLongMapToFile(map, dos);
    }

    public static final boolean writeTLongLongMapToFile(BaseFile f, TLongLongHashMap map) throws IOException {
        DataOutputStream dos = f.getDataOutputStream();
        return writeTLongLongMapToFile(map, dos);
    }


    private static boolean writeTLongLongMapToFile(final TLongLongHashMap map, final DataOutputStream dos) throws IOException {
        dos.writeInt(map.size());
        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeLong(it.value());
        }
        dos.flush();
        dos.close();
        return true;
    }

    public static final TIntLongHashMap getReverseMapTlongIntMap(TLongIntHashMap map) {
        TIntLongHashMap returnMap = new TIntLongHashMap(map.size());
        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            returnMap.put(it.value(), it.key());

        }
        return returnMap;
    }

    /**
     * Write a LongInt hashmap to a file, along with the size entries.
     *
     * @param f
     * @param map
     * @return
     * @throws IOException
     */
    public static final boolean writeTLongIntMapToFile(File f, TLongIntHashMap map) throws IOException {
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        dos.writeInt(map.size());
        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeInt(it.value());
        }
        dos.flush();
        dos.close();
        return true;
    }

    public static final void merge(TLongIntHashMap target, TLongIntHashMap map) {

        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            target.put(it.key(), it.value());
        }
    }

    /**
     * Load a file into a hashtable by first translating the file entries into fp64 values. used for such things as an
     * identity apply
     *
     * @param f
     * @return true if the file was loaded / false if file was not found
     */
    public static final boolean loadTextFileAsFP64(File f, TLongIntHashMap map, int targetValue) {
        if (!FileUtil.nullOrNotExist(f)) {
            try {
                Iterator<String> iter = FileUtil.getLineReaderIteratorFromFile(f);
                while (iter.hasNext()) {
                    String token = iter.next();
                    token = token.toLowerCase();
                    long fp = FPHash64.getFP(token);
                    map.put(fp, targetValue);
                }
                return true;
            } catch (FileNotFoundException e) {
                Log.util.error("%s %e", e, e);
            }
        }
        return false;
    }

    public static final TIntLongHashMap getTIntLongMapFromFile(File f, TIntLongHashMap map)
            throws IOException {
        DataInputStream dis = FileUtil.getDataInputStreamForFile(f);
        int size = dis.readInt();
        if (map == null) {
            map = new TIntLongHashMap(size);
        }
        int key;
        long val;
        for (int i = 0; i < size; i++) {
            key = dis.readInt();
            val = dis.readLong();
            map.put(key, val);
        }
        return map;
    }

    public static final TIntIntHashMap getTIntIntMapFromFile(File f, TIntIntHashMap map)
            throws IOException {
        DataInputStream dis = FileUtil.getDataInputStreamForFile(f);
        int size = dis.readInt();
        if (map == null) {
            map = new TIntIntHashMap(size);
        }
        int key;
        int val;
        for (int i = 0; i < size; i++) {
            key = dis.readInt();
            val = dis.readInt();
            map.put(key, val);
        }
        return map;
    }

    public static final TLongIntHashMap getTLongIntFromFile(File f, TLongIntHashMap map) throws IOException {
        DataInputStream dis = FileUtil.getDataInputStreamForFile(f);
        int size = dis.readInt();
        if (map == null) {
            map = new TLongIntHashMap(size);
        }
        long hash;
        int docId;
        for (int i = 0; i < size; i++) {
            hash = dis.readLong();
            docId = dis.readInt();
            map.put(hash, docId);
        }
        return map;
    }

    public static final TLongLongHashMap getTLongLongFromFile(File f, TLongLongHashMap map) throws IOException {
        DataInputStream dis = FileUtil.getDataInputStreamForFile(f);
        return gettLongLongHashMap(map, dis);
    }

    public static final TLongLongHashMap getTLongLongFromFile(BaseFile f, TLongLongHashMap map) throws IOException {
        DataInputStream dis = f.getDataInputStream();
        return gettLongLongHashMap(map, dis);
    }

    private static TLongLongHashMap gettLongLongHashMap(TLongLongHashMap map, final DataInputStream dis) throws IOException {
        int size = dis.readInt();
        if (map == null) {
            map = new TLongLongHashMap(size);
        }
        long hash;
        long val;
        for (int i = 0; i < size; i++) {
            hash = dis.readLong();
            val = dis.readLong();
            map.put(hash, val);
        }
        return map;
    }

    public static final TLongLongHashMap getReverseMapTLongLongMap(TLongLongHashMap map) {
        TLongLongHashMap returnMap = new TLongLongHashMap(map.size());
        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            returnMap.put(it.value(), it.key());

        }
        return returnMap;
    }

    public static final TIntLongHashMap getReverseMapTLongIntMap(TLongIntHashMap map) {
        TIntLongHashMap returnMap = new TIntLongHashMap(map.size());
        for (TLongIntIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            returnMap.put(it.value(), it.key());

        }
        return returnMap;
    }

    public static final long getMaxValueFromTLongLongMap(TLongLongHashMap map) {
        long maxVal = Long.MIN_VALUE;
        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            long val = it.value();
            if (val > maxVal) {
                maxVal = val;
            }
        }
        return maxVal;
    }
}
