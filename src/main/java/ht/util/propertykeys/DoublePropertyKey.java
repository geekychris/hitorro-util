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
