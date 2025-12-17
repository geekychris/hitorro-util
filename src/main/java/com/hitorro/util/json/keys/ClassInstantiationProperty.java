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
package com.hitorro.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.function.Function;

public class ClassInstantiationProperty<T> extends BaseMappingProperty<T> {
    public ClassInstantiationProperty(final Propaccess access, final String description, final T defaultValue, final Class superC) throws PropertyException {
        super(access, description, defaultValue, new ClassInstantiationMapper(superC));
    }

    public ClassInstantiationProperty(final String access, final String description, final T defaultValue, Class superC) throws PropertyException {
        super(new Propaccess(access), description, defaultValue, new ClassInstantiationMapper(superC));
    }
}

class ClassInstantiationMapper<T> implements Function<JsonNode, T> {
    private Class superC;

    public ClassInstantiationMapper(Class c) {
        this.superC = c;
    }

    @Override
    public T apply(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        String className;
        if (jsonNode.isTextual()) {
            className = jsonNode.textValue();
        } else {
            className = jsonNode.asText();
        }
        return (T) ClassUtil.getInstanceSwallowError(className, superC);
    }
}