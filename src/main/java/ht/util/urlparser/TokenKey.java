package ht.util.urlparser;

import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 4, 2008 Time: 1:07:58 PM
 */
public class TokenKey {
    protected String tld;
    protected long hash;
    protected int id;


    public TokenKey(String t, int id) {
        tld = t;
        hash = StringUtil.hashStringCaseFree(tld);
        this.id = id;
    }

    public String toString() {
        return Fmt.S("%s: %s", tld, hash);
    }

    public int getId() {
        return id;
    }

    public String getToken() {
        return tld;
    }

    public long getHash() {
        return hash;
    }
}