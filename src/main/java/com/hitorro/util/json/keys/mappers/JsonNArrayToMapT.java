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
package com.hitorro.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.keys.BaseMappingProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonNArrayToMapT<K, T> implements Mapper<JsonNode, Map<K, T>> {
    private BaseMappingProperty<K> keyMapper;
    private Function<JsonNode, T> mapper;

    public JsonNArrayToMapT(BaseMappingProperty<K> keyMapper, Function<JsonNode, T> mapper) {
        this.keyMapper = keyMapper;
        this.mapper = mapper;
    }

    public Map<K, T> apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isArray()) {
            Map<K, T> map = new HashMap<K, T>();
            int size = jsonNodes.size();
            for (int i = 0; i < size; i++) {
                JsonNode elem = jsonNodes.get(i);
                K k = keyMapper.apply(elem);
                T t = mapper.apply(elem);
                map.put(k, t);
            }
            return map;
        }
        return null;
    }
}
