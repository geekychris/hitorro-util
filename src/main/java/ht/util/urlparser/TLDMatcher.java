package ht.util.urlparser;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 4, 2008 Time: 12:49:12 PM
 */
public class TLDMatcher<O extends TokenKey> extends PathComponentMatcher<O> {
    public TLDMatcher(final O unknown, final O... keysIn) {
        super(unknown, keysIn);
    }

    public boolean resetTo(UrlMemoryCursor umc) {
        umc.resetToHost();
        return umc.computeTLD();
    }

    public O match(UrlMemoryCursor umc) {
        if (resetTo(umc)) {

            long hash = umc.getValueHash();
            long t;
            for (int i = 0; i < hashes.length; i++) {
                t = hashes[i];
                if (t == hash) {
                    // hash test check for proper extension test
                    if (umc.isValuePartTokenSameIgnoreCase(keys[i].getToken())) {
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

