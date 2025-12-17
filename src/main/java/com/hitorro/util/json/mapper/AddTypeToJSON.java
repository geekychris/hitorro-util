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
package com.hitorro.util.json.mapper;

import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONMap;
import com.hitorro.util.json.JSONString;

/**
 * Decorate a json apply with the hitorro type string
 */
public class AddTypeToJSON extends BaseMapper<JSONElement, JSONElement> {
    public static final String HTTypeField = "ht_type";
    private JSONString type;

    public AddTypeToJSON(String type) {
        this.type = new JSONString(type);
    }

    @Override
    public JSONElement apply(final JSONElement e) {
        JSONMap map = (JSONMap) e;
        map.put(HTTypeField, type);
        return e;
    }
}
