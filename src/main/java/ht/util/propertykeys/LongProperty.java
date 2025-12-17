package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.Constants;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class LongProperty extends PropertyKey<Long> {
    private long _defaultValue = 0;
    private boolean _haveDefault = false;

    public LongProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            _defaultValue = Long.parseLong(ano.defaultValue());
            _haveDefault = true;
        }
    }

    public LongProperty(String key, String description) {
        super(key, description);
    }

    public LongProperty(String key, String description, long defaultValue) {
        this(key, description);
        _defaultValue = defaultValue;
        _haveDefault = true;
    }

    public String getPropertyType() {
        return "Long";
    }

    public Long apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null && _haveDefault) {
            // we don't validate a default value because our validation just checks for integerness
            return _defaultValue;
        }

        validate(sValue);
        long result = 0;
        try {
            // this should never fail because we've just validated
            result = Long.parseLong(sValue);
        } catch (NumberFormatException nfe) {
            return null;
        }
        return Constants.getLong(result);
    }

    protected void validate(String sValue)
            throws PropertyKeyValidationException {
        try {
            Long.parseLong(sValue);
        } catch (NumberFormatException nfe) {
            throw new PropertyKeyValidationException("Property is not a long", getKey(), sValue);
        }

        return;
    }
}
