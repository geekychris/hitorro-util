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
import ht.util.core.BooleanUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

public class BooleanProperty extends PropertyKey<Boolean> {
    private boolean m_defaultVal;
    private boolean m_mustExist;

    public BooleanProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            m_defaultVal = Boolean.parseBoolean(ano.defaultValue());
            m_mustExist = ano.mustExist();
        }
    }

    public BooleanProperty(String key, String description, boolean mustExist, boolean defaultVal) {
        super(key, description);
        m_defaultVal = defaultVal;
        m_mustExist = mustExist;
    }

    public String getPropertyType() {
        return "Bool";
    }

    public Boolean getValue() {
        return apply(null);
    }

    public Boolean apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null) {
            if (m_mustExist) {
                throw new PropertyKeyValidationException("Property missing", this.m_key, "<<null>>");
            }
            return m_defaultVal;
        }
        validate(sValue);
        boolean bval = BooleanUtil.getBoolean(sValue);
        return bval ? Boolean.TRUE : Boolean.FALSE;
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (m_mustExist || !StringUtil.nullOrEmptyOrBlankString(getValueFromConfig(map))) {
            super.validate(map);
        }
    }

    protected void validate(String sValue)
            throws PropertyKeyValidationException {
        if (!BooleanUtil.isBoolean(sValue)) {
            throw new PropertyKeyValidationException("Property is not a boolean", getKey(), sValue);
        }

        return;
    }
}
