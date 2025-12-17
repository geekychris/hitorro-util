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
import com.hitorro.util.json.keys.mappers.JsonNArrayToMapT;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.Map;
import java.util.function.Function;

public class MapProperty<K, T> extends BaseMappingProperty<Map<K, T>> {
    public MapProperty(final Propaccess access, final String description,
                       final Map<K, T> defaultValue, final BaseMappingProperty<T> keyMapper, final Function<JsonNode, K> mapper) throws PropertyException {
        super(access, description, defaultValue, (Function<JsonNode, Map<K, T>>) new JsonNArrayToMapT(keyMapper, mapper));
    }

    public MapProperty(final String path, final String description,
                       final Map<K, T> defaultValue, final BaseMappingProperty<K> keyMapper, final Function<JsonNode, T> mapper) throws PropertyException {
        super(new Propaccess(path), description, defaultValue, (Function<JsonNode, Map<K, T>>) new JsonNArrayToMapT(keyMapper, mapper));
    }
}


