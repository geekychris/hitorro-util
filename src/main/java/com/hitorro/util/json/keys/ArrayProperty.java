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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by chris on 1/9/17.
 */
public class ArrayProperty<T> extends BaseMappingProperty<List<T>> {
    private Function<JsonNode, T> mappingFunction;

    public ArrayProperty(String path, String description, List<T> defaultValue, Function<JsonNode, T> mappingFunction) {
        super(new Propaccess(path), description, defaultValue, new Json2ListOfF(mappingFunction));
    }

    public ArrayProperty(Propaccess path, String description, List<T> defaultValue, Function<JsonNode, T> mappingFunction) {
        super(path, description, defaultValue, new Json2ListOfF(mappingFunction));
    }
}


class Json2ListOfF<T> implements Function<JsonNode, List<T>> {
    private Function<JsonNode, T> mappingFunction;

    public Json2ListOfF(Function<JsonNode, T> mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    @Override
    public List<T> apply(final JsonNode jsonNode) {
        ArrayList<T> list = new ArrayList();

        if (jsonNode.isArray()) {
            ArrayNode an = (ArrayNode) jsonNode;
            for (JsonNode node : an) {
                list.add(mappingFunction.apply(node));
            }
        } else {
            list.add(mappingFunction.apply(jsonNode));
        }

        return list;
    }
}