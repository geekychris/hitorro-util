package ht.util.core.valuemap;

import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 3, 2006 Time: 11:52:01 AM
 */
public class LabelValueMap extends FlatValueMap {
    // Labels must be unique across versions.
    public boolean isUniqueOverSystemVersions() {
        return true;
    }

    public boolean validate(String key) {
        // all key values are valid as long as they are not null
        return !StringUtil.nullOrEmptyOrBlankString(key);
    }
}
