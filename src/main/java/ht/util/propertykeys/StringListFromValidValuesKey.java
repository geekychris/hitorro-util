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

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;
import java.util.Set;

/**
 * Only allow a value from the listFiles of values
 */
public class StringListFromValidValuesKey extends StringProperty {
    private Set<String> values;

    public StringListFromValidValuesKey(String key,
                                        String description,
                                        String defaultValue,
                                        Set<String> values) {
        super(key, description, defaultValue);
        this.values = values;
    }

    public String getPropertyType() {
        return "List";
    }

    public String apply(Map<String, String> map) {
        String val = getValueFromConfig(map);
        if (val == null && m_notNull == false) {
            if (this.m_resolveVariable && m_defaultValue != null) {
                return JVSProperties.getProperties().resolveJsonVariable(m_defaultValue);
            }
            val = m_defaultValue;
        }
        String tmp = val.toLowerCase();
        if (!values.contains(tmp)) {
            return null;
        }
        return val;
    }

    protected void validate(String sVal)
            throws PropertyKeyValidationException {
        if (sVal == null) {
            if (m_notNull && m_defaultValue == null) {
                throw new PropertyKeyValidationException("Key does not exist", this.m_key, "<<null>>");
            }
            sVal = m_defaultValue;
        }
        String tmp = sVal.toLowerCase();
        if (!values.contains(tmp)) {
            throw new PropertyKeyValidationException("Key not valid value", this.m_key, sVal);
        }
    }

}
