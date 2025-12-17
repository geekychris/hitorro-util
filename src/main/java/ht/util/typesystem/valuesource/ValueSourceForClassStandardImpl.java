package ht.util.typesystem.valuesource;

import ht.util.typesystem.TypeFieldIntf;
import ht.util.typesystem.TypeIntf;
import ht.util.typesystem.annotation.UiProperties;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 21, 2006 Time: 2:29:20 PM
 */
public class ValueSourceForClassStandardImpl implements ValueSourceForClass {
    private TypeIntf m_type;

    public TypeIntf getType() {
        return m_type;
    }

    public void setType(TypeIntf type) {
        m_type = type;
    }

    /**
     * Get all the field names in this type, including superclasses.
     *
     * @return an array of the field names for this type
     */
    public String[] getFieldNames() {
        TypeFieldIntf[] fields = m_type.getTypeFields();
        String[] names = new String[fields.length];
        int ix = 0;
        for (TypeFieldIntf tf : fields) {
            names[ix++] = tf.getName();
        }

        return names;
    }

    public UiProperties getUiProperties(Object obj, String fieldName) {
        UiProperties result = null;
        TypeFieldIntf tf = m_type.getField(fieldName);
        if (tf != null) {
            result = tf.getUiProperties();
        }
        return result;
    }

    public void setValue(Object obj, String fieldName, Object value) {
        setValue(obj, fieldName, value, false);
    }

    @Override
    public void setValue(final Object obj, final String fieldName, final Object value, final boolean ignoreTypeCheck) {
        TypeFieldIntf tf = m_type.getField(fieldName);
        if (ignoreTypeCheck || tf != null) {
            tf.setValue(obj, value);
        }
    }

    public Object getValue(Object obj, String fieldName) {
        Object result = null;
        TypeFieldIntf tf = m_type.getField(fieldName);
        if (tf != null) {
            result = tf.getValue(obj);
        }
        return result;
    }

    @Override
    public Object getValue(final String fieldName) {
        return getValue(this, fieldName);
    }
}
