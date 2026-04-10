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
package com.hitorro.util.propertykeys;

import com.hitorro.util.json.keys.PropertyKeyValidationException;

import java.util.Map;


public class EnumKey<E extends Enum> extends PropertyKey<E> {
    protected E vals[];
    protected String names[];
    protected E defaultVal;

    public EnumKey(String key, String description, E[] vals, E defaultVal) {
        super(key, description);
        this.vals = vals;
        this.defaultVal = defaultVal;
        names = new String[vals.length];
        setNames(vals);
    }

    @Override
    public String getPropertyType() {
        return "Enum";
    }

    private void setNames(E e[]) {
        names = new String[e.length];
        for (int i = 0; i < e.length; i++) {
            names[i] = getName(e[i]);
        }
    }

    public String getName(E e) {
        return e.name().toLowerCase();
    }


    @Override
    public E apply(final Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        return get(sValue);
    }

    private E get(String sValue) {
        if (sValue == null) {
            if (defaultVal != null) {
                return defaultVal;
            }
            return null;
        }
        sValue = sValue.toLowerCase();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(sValue)) {
                return vals[i];
            }
        }
        return null;
    }

    @Override
    protected void validate(final String sVal) throws PropertyKeyValidationException {
        try {

            E e = get(sVal);
            if (e == null) {
                throw new PropertyKeyValidationException("Property is not a valid enumeration", getKey(), sVal);
            }
        } catch (NumberFormatException nfe) {
            throw new PropertyKeyValidationException("Property is not a valid enumeration", getKey(), sVal);
        }

        return;
    }

}
