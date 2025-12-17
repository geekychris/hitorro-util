package ht.util.urlparser;

import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 24, 2005 Time: 6:29:12 PM path to test
 * against.
 */
public class PathMatch {
    protected long[] hashes;
    protected String[] keys;

    public PathMatch(String... keysIn) {
        int size = keysIn.length;
        hashes = new long[size];
        keys = new String[size];
        for (int i = 0; i < size; i++) {
            hashes[i] = StringUtil.hashStringCaseFree(keysIn[i]);
            keys[i] = keysIn[i];
        }
        // dont sort its just a seperated path
    }

    /**
     * Does not assume its at the start of the url.  Continues from current position
     *
     * @param umc
     * @return
     */
    public boolean match(UrlMemoryCursor umc) {
        long hash;
        for (int i = 0; i < hashes.length; i++) {
            if (i != 0) {
                if (!umc.nextToken()) {
                    return false;
                }
            }
            hash = umc.getHash();
            if (hashes[i] != hash) {
                return false;
            }
            if (!umc.isTokenSameIgnoreCase(keys[i])) {
                return false;
            }
        }
        // all parts are same
        return true;
    }

}
