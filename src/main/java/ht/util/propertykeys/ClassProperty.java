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
package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.Log;
import ht.util.core.classes.ClassUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 *
 */
public class ClassProperty<T> extends PropertyKey<Class> {
    private Class requiredSuper = Object.class;
    private Class m_defaultVal;
    private boolean m_mustExist;

    public ClassProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            try {
                m_defaultVal = Class.forName(ano.defaultValue());
                m_mustExist = ano.mustExist();
            } catch (ClassNotFoundException e) {
                Log.util.error("Unable to find class %s %s %e", ano.defaultValue(), e, e);
            }
        }
    }

    public ClassProperty(String key, String description, boolean mustExist, Class c) {
        super(key, description);
        m_defaultVal = c;
        m_mustExist = mustExist;
    }

    public String getPropertyType() {
        return "Bool";
    }

    public Class getValue() {
        return apply(null);
    }

    public T getNewInstanceSwallowError() {
        return getNewInstanceSwallowError(null);
    }


    public T getNewInstanceSwallowError(Map<String, String> map) {
        Class c = apply(map);
        if (c != null) {
            return (T) ClassUtil.getInstanceSwallowError(c, requiredSuper);
        }
        return null;
    }

    public Class apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null) {
            if (m_mustExist) {
                throw new PropertyKeyValidationException("Property missing", this.m_key, "<<null>>");
            }
            return m_defaultVal;
        }
        validate(sValue);
        return ClassUtil.getClassForName(sValue, requiredSuper);
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (m_mustExist || !StringUtil.nullOrEmptyOrBlankString(getValueFromConfig(map))) {
            super.validate(map);
        }
    }

    protected void validate(String sValue) throws PropertyKeyValidationException {
        if (ClassUtil.getClassForName(sValue, requiredSuper) == null) {
            throw new PropertyKeyValidationException("Property is not the correct class", getKey(), sValue);
        }

        return;
    }
}

