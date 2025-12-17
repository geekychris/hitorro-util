/*
    Copyright (c) 2003 - present HiTorro All rights reserved.


    User: chris
*/

package ht.util.typesystem.valuesource;

import ht.util.typesystem.TypeIntf;
import ht.util.typesystem.annotation.UiProperties;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 24, 2006 Time: 12:06:28 PM
 */
public interface ValueSourceForClass {
    TypeIntf getType();

    void setType(TypeIntf type);

    Object getValue(Object obj, String fieldName);

    Object getValue(String fieldName);

    void setValue(Object obj, String fieldName, Object value);

    void setValue(Object ojb, String fieldName, Object value, boolean ignoreTypeCheck);

    String[] getFieldNames();

    UiProperties getUiProperties(Object obj, String fieldName);
}

