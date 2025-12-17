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
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.function.Function;

/**
 * Created by chris on 12/31/16.
 */
public class StaticVarProperty<T> extends BaseMappingProperty<T> {
    public StaticVarProperty(String path, String description, boolean mustExist, T defaultVal, Class requiredSuper) {
        super(new Propaccess(path), description, defaultVal, new SVPM(requiredSuper, path));
    }

    public String getPropertyType() {
        return "Object";
    }
}

class SVPM<T> implements Function<JsonNode, T> {
    private Class superC;
    private String key;

    public SVPM(Class superC, String key) {
        this.superC = superC;
        this.key = key;
    }

    public T apply(JsonNode node) {
        String sValue;

        if (node.isTextual()) {
            sValue = node.textValue();
        } else {
            sValue = node.asText();
        }

        return (T) com.hitorro.util.propertykeys.StaticVarProperty.getValidated(sValue, superC, key);
    }
}