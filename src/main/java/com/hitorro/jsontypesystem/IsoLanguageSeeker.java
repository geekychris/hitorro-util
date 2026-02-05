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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;

public class IsoLanguageSeeker implements IndexSeeker {
    public static StringProperty langKey = new StringProperty("lang", "", null);

    public int getIndex(ArrayNode node, Propaccess access, int depth, String value, JVS jvs) {
        if (node == null) {
            return -1;
        }
        for (int i = 0; i < node.size(); i++) {
            String v = langKey.apply(node.get(i));
            if (value.equals(v)) {
                return i;
            }
        }
        if (value.length() > 2) {
            value = value.substring(0, 2);
        }
        for (int i = 0; i < node.size(); i++) {
            String v = langKey.apply(node.get(i));
            if (value.equals(v)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean init(final JsonNode node) {
        return false;
    }

    @Override
    public JsonNode createElement(String indexValue) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("lang", indexValue);
        return node;
    }
}
