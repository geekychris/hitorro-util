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
package com.hitorro.jsontypesystem.dynamic.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hitorro.util.core.iterator.mappers.JsonInitableMapper;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseJsonInteropMapper extends JsonInitableMapper<JsonNode, JsonNode> {
    public String toStringFromJsonNode(JsonNode node) {
        return node.textValue();
    }

    public String stringMap(String node) {
        return null;
    }

    public long string2longMap(String s) {
        return 0;
    }

    public JsonNode toJsonNodeFromString(String s) {
        return JsonNodeFactory.instance.textNode(s);
    }

    public ArrayNode string2JsonVec(List<String> l) {
        ArrayNode an = JsonNodeFactory.instance.arrayNode();
        for (String s : l) {
            an.add(toJsonNodeFromString(s));
        }
        return an;
    }

    public List<String> json2StringVec(ArrayNode an) {
        ArrayList<String> l = new ArrayList<>();
        for (JsonNode e : an) {
            l.add(stringMap(e.textValue()));
        }
        return l;
    }


    public JsonNode applyString2String(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isArray()) {
            return string2JsonVec(json2StringVec((ArrayNode) jsonNode));
        } else {
            return toJsonNodeFromString(toStringFromJsonNode(jsonNode));
        }
    }


    public JsonNode applyString2long(final JsonNode jsonNode) {
        if (jsonNode == null) {
            return null;
        }
        if (jsonNode.isArray()) {
            ArrayNode an = JsonNodeFactory.instance.arrayNode();
            for (JsonNode elem : jsonNode) {
                an.add(string2longMap(toStringFromJsonNode(elem)));
            }
            return an;
        } else {
            return JsonNodeFactory.instance.numberNode(string2longMap(toStringFromJsonNode(jsonNode)));
        }
    }
}
