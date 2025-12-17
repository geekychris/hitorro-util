package ht.util.urlparser;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 23, 2005 Time: 10:28:24 AM Match the
 * extension of a url
 */
public class ExtensionMatcher extends TokenMatch {
    public ExtensionMatcher(String... args) {
        super(args);
    }

    public boolean match(UrlMemoryCursor umc) {
        if (umc.hasExtension()) {
            long hash = umc.getValueHash();
            long t;
            for (int i = 0; i < hashes.length; i++) {
                t = hashes[i];
                if (t == hash) {
                    // hash test check for proper extension test
                    if (umc.isValuePartTokenSameIgnoreCase(keys[i])) {
                        return true;
                    }

                }
                if (t > hash) {
                    // didnt find a test
                    return false;
                }
            }
        }
        return false;
    }
}
