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
package com.hitorro.util.propertykeys;

import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.KeyValue;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.json.keys.PropertyKeyValidationException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Property Key is a wrapper around fetching a key from the registry / config. This allows validation and appropriate
 * convertion of the key.
 *
 * @author chris
 */
public abstract class PropertyKey<T> implements Function<Map<String, String>, T> {
    protected String m_key;
    protected String m_description;
    protected boolean m_resolveVariable = false;

    public PropertyKey(DebugArgAno ano) {

    }

    public PropertyKey(String key, String description) {
        m_key = key;
        m_description = description;
    }

    public abstract String getPropertyType();

    /**
     * Indicates if this property key provides a listFiles of valid values
     *
     * @return
     */
    public boolean getHasValidationList() {
        return false;
    }

    /**
     * List of valid values for properties that should only allow valid values in the system. The Key is the raw key
     * (that would be entered in the command line). The Value is the description that would be used in a dropdown listFiles.
     *
     * @return
     */
    public List<KeyValue> getValidationList() {
        return null;
    }

    /**
     * The canonical key of the property.
     *
     * @return
     */
    public String getKey() {
        return m_key;
    }

    /**
     * Hidden method used by subclasses to get the property out of the properties hierarchy.
     *
     * @return
     */
    protected String getValueFromConfig(Map<String, String> map) {
        // fetch the raw String value
        String s;
        if (map == null) {
            s = HTProperties.getProperties().get(getKey());
        } else {
            s = map.get(m_key);
        }
        if (this.m_resolveVariable && s != null) {
            return GlobalProperties.resolveJsonVariable(s);
        }
        return s;
    }

    public abstract T apply(Map<String, String> map);

    /**
     * English description of the purpose of the property.
     *
     * @return
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * Used for debug AND must be overriden to get the correct default values.
     */
    public String toString(Map<String, String> map) {
        return getValueFromConfig(map);
    }

    /**
     * Throw an exception if the value associated with this key is invalid
     *
     * @throws PropertyKeyValidationException
     */
    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        String sVal = getValueFromConfig(map);
        validate(sVal);
    }

    /**
     * Throw an exception if the value does not pass validation for this type
     *
     * @param sVal The string to validate
     * @throws PropertyKeyValidationException
     */
    protected abstract void validate(String sVal)
            throws PropertyKeyValidationException;
}
