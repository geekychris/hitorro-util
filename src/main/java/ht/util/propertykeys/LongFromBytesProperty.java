package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.Constants;
import ht.util.json.keys.PropertyKeyValidationException;

import java.text.ParseException;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 9:27:28 AM
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
