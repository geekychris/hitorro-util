/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.Constants;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.Map;

/**
 * Boolean property values.
 *
 * @author chris
 */
public class IntProperty extends PropertyKey<Integer> {
    private int _defaultValue = 0;
    private boolean _haveDefault = false;

    public IntProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            _defaultValue = Integer.parseInt(ano.defaultValue());
            _haveDefault = true;
        }
    }

    public IntProperty(String key, String description) {
        super(key, description);
    }

    public IntProperty(String key, String description, int defaultValue) {
        this(key, description);
        _defaultValue = defaultValue;
        _haveDefault = true;
    }

    public String getPropertyType() {
        return "Integer";
    }

    public Integer getValue() {
        return apply(null);
    }

    public Integer apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null && _haveDefault) {
            // we don't validate a default value because our validation just checks for integerness
            return _defaultValue;
        }

        validate(sValue);
        int result = 0;
        try {
            // this should never fail because we've just validated
            result = Integer.parseInt(sValue);
        } catch (NumberFormatException nfe) {
            return null;
        }
        return Constants.getInteger(result);
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
