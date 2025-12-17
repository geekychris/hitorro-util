package ht.util.core.valuemap;

import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 6, 2006 Time: 4:18:31 PM
 */
public class NonValidatedFlatValueMap<E> extends FlatValueMap<E> {
    public boolean validate(String key) {
        return !StringUtil.nullOrEmptyOrBlankString(key);
    }
}
