package ht.util.urlparser;

import java.util.Arrays;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 24, 2005 Time: 11:53:36 AM Case free
 * collector of parameters we care to extract from a url using the UrlCursor.  This should be a relatively efficient
 * linear extraction process as long as the parameters expected are finite.  Other collectors should be used if key
 * value pairs are unknown in advance.
 */
public class UrlCursorParameters {
    // sorted by hash
    private UrlCursorParameter params[];

    public UrlCursorParameters(UrlCursorParameter... params) {
        this.params = params;
        Arrays.sort(params);
    }

    /**
     * Scan the url picking out any of the
     *
     * @param umc
     * @return
     */
    public boolean collect(UrlMemoryCursor umc) {
        boolean found = false;
        for (UrlCursorParameter p : params) {
            p.reset();
        }
        if (!umc.resetToFirstArg()) {
            return false;
        }
        long hash;
        do {
            hash = umc.getKeyHash();
            for (UrlCursorParameter p : params) {
                if (hash < p.hash) {
                    break;
                }
                if (hash == p.hash) {
                    // ensure strings test (hash is not enough)
                    if (umc.isKeyPartTokenSameIgnoreCase(p.token)) {
                        // found a test
                        String val = umc.getValuePartOfArgToken();
                        p.setValue(val);
                        found = true;
                        break;
                    }
                }
            }
        }
        while (umc.nextToken());
        return found;
    }
}