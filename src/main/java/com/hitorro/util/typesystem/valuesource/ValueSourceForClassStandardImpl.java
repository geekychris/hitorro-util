/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.typesystem.valuesource;

import com.hitorro.util.typesystem.TypeFieldIntf;
import com.hitorro.util.typesystem.TypeIntf;
import com.hitorro.util.typesystem.annotation.UiProperties;

/**
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
