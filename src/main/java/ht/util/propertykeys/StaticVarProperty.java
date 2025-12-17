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
import ht.util.core.classes.ClassUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * provide a variable on a class as such: a.b.c.Class#var
 */
public class StaticVarProperty<T> extends PropertyKey<T> {
    private Class requiredSuper = Object.class;
    private T m_defaultVal;
    private boolean m_mustExist;

    public StaticVarProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            m_defaultVal = (T) getValidated(ano.defaultValue(), requiredSuper, this.getKey());
            m_mustExist = ano.mustExist();
        }
    }

    public StaticVarProperty(String key, String description, boolean mustExist, T defaultVal, Class requiredSuper) {
        super(key, description);
        m_defaultVal = defaultVal;
        m_mustExist = mustExist;
        this.requiredSuper = requiredSuper;
    }

    public static Object getValidated(String sValue, Class requiredSuper, String key) {
        String parts[] = StringUtil.tokenizeFromSingleChar(sValue, "#");
        if (parts == null) {
            throw new PropertyKeyValidationException("Property is not classname#staticvariable", key, sValue);
        }
        if (parts.length == 1) {
            // we must be handling just a class name
            Class c = ClassUtil.getClassForName(parts[0], Object.class);
            if (c != null) {
                return ClassUtil.getInstanceSwallowError(c, requiredSuper);
            }
        }
        Class c = ClassUtil.getClassForName(parts[0], Object.class);
        if (c == null) {
            throw new PropertyKeyValidationException("Property is not a boolean", key, sValue);
        }
        try {
            Field field = c.getField(parts[1]);
            try {
                Object o = field.get(null);
                if (!ClassUtil.isSubClass(o.getClass(), requiredSuper)) {
                    throw new PropertyKeyValidationException("Unknown subclass for variable value %s %s", key, o.getClass().getCanonicalName());
                }
                return o;
            } catch (IllegalAccessException e) {
                throw new PropertyKeyValidationException("Unknown field value %s %s", key, e.toString());
            }
        } catch (NoSuchFieldException e) {
            throw new PropertyKeyValidationException("Unknown field %s %s", key, e.toString());
        }
    }

    public String getPropertyType() {
        return "Object";
    }

    public T getValue() {
        return apply(null);
    }

    public T apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null) {
            if (m_mustExist) {
                throw new PropertyKeyValidationException("Property missing", this.m_key, "<<null>>");
            }
            return m_defaultVal;
        }
        return (T) getValidated(sValue, requiredSuper, getKey());
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (m_mustExist || !StringUtil.nullOrEmptyOrBlankString(getValueFromConfig(map))) {
            super.validate(map);
        }
    }

    protected void validate(String sValue) throws PropertyKeyValidationException {
        getValidated(sValue, requiredSuper, getKey());
    }
}


