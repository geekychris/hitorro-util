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
package com.hitorro.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.PropertyKeyValidationException;
import com.hitorro.util.json.keys.StringProperty;

import java.lang.reflect.Field;

public class JsonNodeClassMapper<T> implements Mapper<JsonNode, T> {
    public static StringProperty classKey = new StringProperty("class", "", null);

    private Class requiredSuper;
    private Class defaultClass;
    private String key;

    public JsonNodeClassMapper(Class requiredSuper, String key, Class defaultClass) {
        this.requiredSuper = requiredSuper;
        this.defaultClass = defaultClass;
        this.key = key;
    }

    @Override
    public T apply(final JsonNode s) {
        return getValidated(classKey.apply(s), requiredSuper, key);
    }

    public T getValidated(String sValue, Class requiredSuper, String key) {
        // Resolve symbolic names to FQN class names via the implementation registry
        sValue = ImplementationRegistry.getMe().resolve(sValue);
        String parts[] = StringUtil.tokenizeFromSingleChar(sValue, "#");
        if (ArrayUtil.nullOrEmpty(parts)) {
            if (defaultClass != null) {
                return (T) ClassUtil.getInstanceSwallowError(defaultClass, requiredSuper);
            } else {
                return null;
            }
        }
        if (parts.length == 1) {
            // we must be handling just a class name
            Class c = ClassUtil.getClassForName(parts[0], Object.class);
            if (c != null) {
                return (T) ClassUtil.getInstanceSwallowError(c, requiredSuper);
            }
        }
        Class c = ClassUtil.getClassForName(parts[0], Object.class);
        if (c == null) {
            throw new PropertyKeyValidationException("Property unable to find class", key, sValue);
        }
        try {
            Field field = c.getField(parts[1]);
            try {
                Object o = field.get(null);
                if (!ClassUtil.isSubClass(o.getClass(), requiredSuper)) {
                    throw new PropertyKeyValidationException("Unknown subclass for variable value %s %s", key, o.getClass().getCanonicalName());
                }
                return (T) o;
            } catch (IllegalAccessException e) {
                throw new PropertyKeyValidationException("Unknown field value %s %s", key, e.toString());
            }
        } catch (NoSuchFieldException e) {
            throw new PropertyKeyValidationException("Unknown field %s %s", key, e.toString());
        }
    }
}

