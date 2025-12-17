package ht.util.urlparser;

import ht.util.core.string.StringUtil;
import ht.util.core.tandemarrays.TandemLongArraySorter;
import ht.util.core.tandemarrays.TandemObjectArraySorterPeer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 23, 2005 Time: 9:43:15 PM Look for token
 * matches.  They must be complete, but can be any case. We do not expect the matcher to advance.  The cursor must have
 * been set correctly beforehand
 */
public class TokenMatch {
    protected long[] hashes;
    protected String[] keys;

    public TokenMatch(String... keysIn) {
        int size = keysIn.length;
        hashes = new long[size];
        keys = new String[size];
        for (int i = 0; i < size; i++) {
            hashes[i] = StringUtil.hashStringCaseFree(keysIn[i]);
            keys[i] = keysIn[i];
        }
        // sort this puppy so that we can efficiently go down the hash listFiles.
        TandemLongArraySorter sorter = new TandemLongArraySorter();
        TandemObjectArraySorterPeer peer = new TandemObjectArraySorterPeer();
        peer.set(keys);
        sorter.sort(hashes, peer);
    }

    public boolean match(UrlMemoryCursor umc) {
        long hash = umc.getHash();
        long t;
        for (int i = 0; i < hashes.length; i++) {
            t = hashes[i];
            if (t == hash) {
                // hash test check for proper extension test
                if (umc.isTokenSameIgnoreCase(keys[i])) {
                    return true;
                }

            }
            if (t > hash) {
                // didnt find a test
                return false;
            }
        }

        return false;
    }
}
