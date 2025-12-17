/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.BooleanUtil;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 * Boolean property values.
 *
 * @author chris
 */
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
