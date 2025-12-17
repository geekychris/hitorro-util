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
import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;


public class StringProperty extends PropertyKey<String> {
    protected boolean m_notNull;
    protected String m_defaultValue = null;

    public StringProperty(String key, String description, boolean notNull) {
        super(key, description);
        m_notNull = notNull;
    }

    public StringProperty(DebugArgAno ano) {
        this(ano.keyName(), ano.description(), ano.defaultValue());
    }

    public StringProperty(String key, String description, String defaultValue) {
        super(key, description);
        m_notNull = false;
        m_defaultValue = defaultValue;
    }

    public String getPropertyType() {
        return "String";
    }

    public String getValue() {
        return apply(null);
    }

    public String apply(Map<String, String> map) {
        String val = getValueFromConfig(map);
        if (val == null && m_notNull == false) {
            if (this.m_resolveVariable && m_defaultValue != null) {
                return JVSProperties.getProperties().resolveJsonVariable(m_defaultValue);
            }
            return m_defaultValue;
        }
        return val;
    }

    protected void validate(String sVal)
            throws PropertyKeyValidationException {
        if (sVal == null) {
            if (m_notNull && m_defaultValue == null) {
                throw new PropertyKeyValidationException("Key does not exist", this.m_key, "<<null>>");
            }
        }
    }

}
