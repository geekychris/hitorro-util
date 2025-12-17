package ht.util.urlparser;


import gnu.trove.map.hash.TIntObjectHashMap;
import ht.util.core.tandemarrays.TandemLongArraySorter;
import ht.util.core.tandemarrays.TandemObjectArraySorterPeer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class PathComponentMatcher<O extends TokenKey> {
    protected long[] hashes;
    protected O[] keys;
    protected O unknown;
    protected TIntObjectHashMap map;


    public PathComponentMatcher(O unknown, O... keysIn) {
        this.unknown = unknown;
        int size = keysIn.length;
        hashes = new long[size];
        keys = keysIn;
        for (int i = 0; i < size; i++) {
            hashes[i] = keysIn[i].getHash();
        }
        // sort this puppy so that we can efficiently go down the hash listFiles.
        TandemLongArraySorter sorter = new TandemLongArraySorter();
        TandemObjectArraySorterPeer peer = new TandemObjectArraySorterPeer();
        peer.set(keys);
        sorter.sort(hashes, peer);


        map = new TIntObjectHashMap();
        {
            for (O key : keys) {
                map.put(key.getId(), key);
            }
        }
    }

    public TIntObjectHashMap getIdMap() {
        return map;
    }

    public boolean resetTo(UrlMemoryCursor umc) {
        return true;
    }

    public O match(UrlMemoryCursor umc) {
        if (resetTo(umc)) {

            long hash = umc.getHash();
            long t;
            for (int i = 0; i < hashes.length; i++) {
                t = hashes[i];
                if (t == hash) {
                    // hash test check for proper extension test
                    if (umc.isTokenSameIgnoreCase(keys[i].getToken())) {
                        return keys[i];
                    }
                }
                if (t > hash) {
                    // didnt find a test
                    return unknown;
                }
            }
        }

        return unknown;
    }
}
