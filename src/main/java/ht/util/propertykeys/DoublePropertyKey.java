package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class DoublePropertyKey extends PropertyKey<Double> {
    private double _defaultValue = 0;
    private boolean _haveDefault = false;

    public DoublePropertyKey(String key, String description) {
        super(key, description);
    }

    public DoublePropertyKey(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            _defaultValue = Double.parseDouble(ano.defaultValue());
            _haveDefault = true;
        }
    }

    public DoublePropertyKey(String key, String description, double defaultValue) {
        this(key, description);
        _defaultValue = defaultValue;
        _haveDefault = true;
    }

    public String getPropertyType() {
        return "Double";
    }

    public Double getValue() {
        return apply(null);
    }

    public Double apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null && _haveDefault) {
            // we don't validate a default value because our validation just checks for integerness
            return _defaultValue;
        }

        validate(sValue);
        int result = 0;
        try {
            // this should never fail because we've just validated
            return Double.parseDouble(sValue);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    protected void validate(String sValue)
            throws PropertyKeyValidationException {
        try {
            Double.parseDouble(sValue);
        } catch (NumberFormatException nfe) {
            throw new PropertyKeyValidationException("Property is not a double", getKey(), sValue);
        }

        return;
    }


}
