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


import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.map.hash.TLongLongHashMap;
import com.hitorro.util.io.FileUtil;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * <p/>
 * Map of external hash to an internal document id.  Document ID'saveMap currently do not get re-used.
 */
public class HashToIdAllocatingMap {
    private TLongLongHashMap newMap = new TLongLongHashMap();
    private TLongLongHashMap existingIDMap = new TLongLongHashMap();
    private long currentHighValue;

    private TLongLongHashMap reverseNewMap = new TLongLongHashMap();
    private TLongLongHashMap reverseIdMap = new TLongLongHashMap();
    private boolean doDocIdTranslation = true;


    private HashToIdAllocatingMap() {

    }

    public HashToIdAllocatingMap(boolean doDocIdTranslation) {
        this.doDocIdTranslation = doDocIdTranslation;
    }

    public boolean getHashExists(long hash) {
        if (doDocIdTranslation) {
            if (newMap.contains(hash)) {
                return true;
            }
            return existingIDMap.contains(hash);
        } else {
            return true;
        }
    }

    public boolean getDocIdExists(int docId) {
        if (doDocIdTranslation) {
            if (reverseNewMap.contains(docId)) {
                return true;
            }
            return reverseIdMap.contains(docId);
        } else {
            // probably not the right thing todo
            return true;
        }
    }

    public int getSize() {
        return newMap.size() + existingIDMap.size();
    }

    /**
     * get a document id from the hash.  If this hash is currently not known, then it is added by using the current high
     * water mark computed when the hash table was initially loaded from disk.
     *
     * @param hash
     * @return
     */
    public long getDocIdWithIDAllocation(long hash) {
        if (!doDocIdTranslation) {
            return hash;
        }
        long returnMe = 0;
        returnMe = existingIDMap.get(hash);
        if (returnMe != 0) {
            return returnMe;
        }

        returnMe = newMap.get(hash);
        if (returnMe != 0) {
            return returnMe;
        }

        return getNewDocId(hash);
    }

    /**
     * Called on rollback to remove any additions
     */
    public void flushAdds() {
        newMap.clear();
    }

    /**
     * get the docId from hash.  Should be used at query time as the new id's have not been commited to the query
     * version of the index.
     *
     * @param hash
     * @return
     */
    public long getDocId(long hash) {
        if (!doDocIdTranslation) {
            return hash;
        }
        return existingIDMap.get(hash);
    }

    /**
     * Currently this is derived hashtable from the data docid->hash Used to get the external key from the interna
     * docid.
     *
     * @param docId
     * @return hash code, or 0 if not found.
     */
    public long getHashFromDocId(long docId) {
        if (!doDocIdTranslation) {
            return docId;
        }

        long returnMe = 0;
        returnMe = reverseIdMap.get(docId);
        if (returnMe != 0) {
            return returnMe;
        }

        return reverseNewMap.get(docId);
    }

    public boolean load(File f) throws IOException {
        if (!doDocIdTranslation) {
            // do nothing as we are not translating
            return true;
        }
        existingIDMap = TroveMapUtil.getTLongLongFromFile(f, existingIDMap);
        currentHighValue = TroveMapUtil.getMaxValueFromTLongLongMap(existingIDMap);
        reverseIdMap = TroveMapUtil.getReverseMapTLongLongMap(existingIDMap);
        return true;
    }

    /**
     * Save operation saves both tiers of the hashtable and as a side effect, merges both tiers together it is assumed
     * that concurrent access is not occuring during this operation.
     *
     * @param f
     * @return
     * @throws IOException
     */
    public boolean save(File f) throws IOException {
        if (!doDocIdTranslation) {
            // do nothing as we are not translating
            return true;
        }
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        int size = newMap.size() + existingIDMap.size();
        dos.writeInt(size);
        saveMap(newMap, dos);
        saveMap(existingIDMap, dos);
        MapUtil.merge(existingIDMap, newMap);
        newMap.clear();
        dos.flush();
        dos.close();
        return true;
    }


    private long getNewDocId(long hash) {
        currentHighValue++;
        // put (hash->docId)
        newMap.put(hash, currentHighValue);
        // put the reverse (docid->hash)
        reverseNewMap.put(currentHighValue, hash);
        return currentHighValue;
    }


    private void saveMap(TLongLongHashMap map, DataOutputStream dos) throws IOException {

        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeLong(it.value());
        }
    }
}