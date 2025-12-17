/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.propertykeys;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;


/**
 * Property of type String. String properties have no validation.
 *
 * @author chris
 */
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
