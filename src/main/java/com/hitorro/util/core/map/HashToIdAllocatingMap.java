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
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 15, 2005 Time: 9:16:17 PM
 * <p/>
 * Map of external hash to an internal document id.  Document ID'saveMap currently do not get re-used.
 */
public class HashToIdAllocatingMap {
    private TLongLongHashMap m_newMap = new TLongLongHashMap();
    private TLongLongHashMap m_existingIDMap = new TLongLongHashMap();
    private long m_currentHighValue;

    private TLongLongHashMap m_reverseNewMap = new TLongLongHashMap();
    private TLongLongHashMap m_reverseIdMap = new TLongLongHashMap();
    private boolean m_doDocIdTranslation = true;


    private HashToIdAllocatingMap() {

    }

    public HashToIdAllocatingMap(boolean doDocIdTranslation) {
        m_doDocIdTranslation = doDocIdTranslation;
    }

    public boolean getHashExists(long hash) {
        if (m_doDocIdTranslation) {
            if (m_newMap.contains(hash)) {
                return true;
            }
            return m_existingIDMap.contains(hash);
        } else {
            return true;
        }
    }

    public boolean getDocIdExists(int docId) {
        if (m_doDocIdTranslation) {
            if (m_reverseNewMap.contains(docId)) {
                return true;
            }
            return m_reverseIdMap.contains(docId);
        } else {
            // probably not the right thing todo
            return true;
        }
    }

    public int getSize() {
        return m_newMap.size() + m_existingIDMap.size();
    }

    /**
     * get a document id from the hash.  If this hash is currently not known, then it is added by using the current high
     * water mark computed when the hash table was initially loaded from disk.
     *
     * @param hash
     * @return
     */
    public long getDocIdWithIDAllocation(long hash) {
        if (!m_doDocIdTranslation) {
            return hash;
        }
        long returnMe = 0;
        returnMe = m_existingIDMap.get(hash);
        if (returnMe != 0) {
            return returnMe;
        }

        returnMe = m_newMap.get(hash);
        if (returnMe != 0) {
            return returnMe;
        }

        return getNewDocId(hash);
    }

    /**
     * Called on rollback to remove any additions
     */
    public void flushAdds() {
        m_newMap.clear();
    }

    /**
     * get the docId from hash.  Should be used at query time as the new id's have not been commited to the query
     * version of the index.
     *
     * @param hash
     * @return
     */
    public long getDocId(long hash) {
        if (!m_doDocIdTranslation) {
            return hash;
        }
        return m_existingIDMap.get(hash);
    }

    /**
     * Currently this is derived hashtable from the data docid->hash Used to get the external key from the interna
     * docid.
     *
     * @param docId
     * @return hash code, or 0 if not found.
     */
    public long getHashFromDocId(long docId) {
        if (!m_doDocIdTranslation) {
            return docId;
        }

        long returnMe = 0;
        returnMe = m_reverseIdMap.get(docId);
        if (returnMe != 0) {
            return returnMe;
        }

        return m_reverseNewMap.get(docId);
    }

    public boolean load(File f) throws IOException {
        if (!m_doDocIdTranslation) {
            // do nothing as we are not translating
            return true;
        }
        m_existingIDMap = TroveMapUtil.getTLongLongFromFile(f, m_existingIDMap);
        m_currentHighValue = TroveMapUtil.getMaxValueFromTLongLongMap(m_existingIDMap);
        m_reverseIdMap = TroveMapUtil.getReverseMapTLongLongMap(m_existingIDMap);
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
        if (!m_doDocIdTranslation) {
            // do nothing as we are not translating
            return true;
        }
        DataOutputStream dos = FileUtil.getDataOutputStreamForFile(f);
        int size = m_newMap.size() + m_existingIDMap.size();
        dos.writeInt(size);
        saveMap(m_newMap, dos);
        saveMap(m_existingIDMap, dos);
        MapUtil.merge(m_existingIDMap, m_newMap);
        m_newMap.clear();
        dos.flush();
        dos.close();
        return true;
    }


    private long getNewDocId(long hash) {
        m_currentHighValue++;
        // put (hash->docId)
        m_newMap.put(hash, m_currentHighValue);
        // put the reverse (docid->hash)
        m_reverseNewMap.put(m_currentHighValue, hash);
        return m_currentHighValue;
    }


    private void saveMap(TLongLongHashMap map, DataOutputStream dos) throws IOException {

        for (TLongLongIterator it = map.iterator(); it.hasNext(); ) {
            it.advance();
            dos.writeLong(it.key());
            dos.writeLong(it.value());
        }
    }
}