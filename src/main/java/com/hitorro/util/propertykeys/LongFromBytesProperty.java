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

import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.Constants;
import com.hitorro.util.json.keys.PropertyKeyValidationException;

import java.text.ParseException;
import java.util.Map;

/**
 */
public class LongFromBytesProperty extends PropertyKey<Long> {
    private long m_defaultValue = 0;
    private boolean m_haveDefault = false;

    public LongFromBytesProperty(String key, String description) {
        super(key, description);
    }

    public LongFromBytesProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            m_defaultValue = Integer.parseInt(ano.defaultValue());
            m_haveDefault = true;
        }
    }

    public LongFromBytesProperty(String key, String description, int defaultValue) {
        this(key, description);
        m_defaultValue = defaultValue;
        m_haveDefault = true;
    }

    public String getPropertyType() {
        return "Bytes";
    }

    public long getLongValue(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null && m_haveDefault) {
            // we don't validate a default value because our validation just checks for integerness
            return m_defaultValue;
        }

        validate(sValue);
        long result = 0;
        try {
            // this should never fail because we've just validated
            result = Constants.getBytesFromString(sValue);
        } catch (ParseException nfe) {
        }
        return result;
    }

    public Long apply(Map<String, String> map) {
        long ival = getLongValue(map);
        return Constants.getLong(ival);
    }

    protected void validate(String sValue)
            throws PropertyKeyValidationException {
        try {
            Integer.parseInt(sValue);
        } catch (NumberFormatException nfe) {
            throw new PropertyKeyValidationException("Property is not an integer", getKey(), sValue);
        }

        return;
    }

}
