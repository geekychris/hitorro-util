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
package com.hitorro.jsontypesystem.dynamic;

//ht.jsontypesystem.dynamic.DynamicMapper

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.iterator.mappers.JsonInitableMapper;
import com.hitorro.util.json.keys.JsonInitableProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;

public class DynamicMapper extends DynamicFieldMapper {
    public static final JsonInitableProperty<JsonInitableMapper<JsonNode, JsonNode>> dynamicFieldMapperKey = new JsonInitableProperty("mapper", "", null, JsonInitableMapper.class, null);
    private JsonInitableMapper<JsonNode, JsonNode> mapper;
    @Override
    public boolean init(final JsonNode node) {
        super.init(node);
        mapper = dynamicFieldMapperKey.apply(node);
        return true;
    }

    public JsonNode map(JVS jvs, Propaccess pa, int depth) {
        JsonNode arr[] = getValues(jvs, pa, depth);
        if (arr.length > 0) {
            return mapper.apply(arr[0]);
        }
        return null;
    }
}
