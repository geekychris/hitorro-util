package ht.util.propertykeys;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.PropertyKeyValidationException;

import java.io.File;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 4, 2006 Time: 9:24:14 PM
 */
public class FileProperty extends PropertyKey<File> {
    protected boolean m_notNull;
    protected String m_defaultValue = null;

    public FileProperty(String key, String description, boolean notNull) {
        super(key, description);
        m_notNull = notNull;
    }

    public FileProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            m_defaultValue = ano.defaultValue();
            m_notNull = false;
        }
    }

    public FileProperty(String key, String description, String defaultValue) {
        super(key, description);
        m_notNull = false;
        m_defaultValue = defaultValue;
    }

    public String getPropertyType() {
        return "String";
    }

    public File getValue() {
        return apply(null);
    }

    public File apply(Map<String, String> map) {
        String val = getValueFromConfig(map);
        if (val == null && m_notNull == false) {
            val = m_defaultValue;
        }
        val = JVSProperties.getProperties().resolveJsonVariable(val);
        if (val == null) {
            return null;
        }
        return new File(val);
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
