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
package com.hitorro.util.json.keys.propaccess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * Context to capture mechanism for creating an element in a node structure (if appropriate
 *
 */
public class PAContext {
    public static PAContext AlwaysCreate = new PAContext();
    public static PAContext NeverCreate = new PAContextNeverCreate();

    public int translateIndexPos(VS jvs, ArrayNode node, Propaccess pa, int depth) {
        return -1;
    }

    public JsonNode getObjectOrArrayIfMissing(VS jvs, JsonNode parent, JsonNode node,
                                              Propaccess pa, int index, boolean secondStage, Propaccess.SetMode setMode) throws PropaccessError {
        int getIndex = index;
        if (attemptCreation(node, setMode)) {
            if (secondStage) {
                if (index < pa.length() - 1) {
                    // skip to the next level to determine what to fill in
                    getIndex++;
                } else {
                    return null;
                }
            }
            JsonNode v;
            if (pa.get(getIndex).isIndexed() || setMode == Propaccess.SetMode.Append) {
                v = getArrayNode(jvs, pa, getIndex, setMode);
            } else {
                v = getObjectNode(jvs, pa, getIndex, setMode);
            }
            if (v != null) {
                return setObjectOrArray(parent, pa, index, v);
            }
        }
        return node;
    }

    protected boolean attemptCreation(final JsonNode node, final Propaccess.SetMode setMode) {
        return (node == null || node.isNull()) && setMode != Propaccess.SetMode.No;
    }


    private JsonNode setObjectOrArray(final JsonNode parent, final Propaccess pa,
                                      final int index, final JsonNode v) throws PropaccessError {
        if (parent.isObject()) {
            if (!pa.get(index).hasName()) {
                throw new PropaccessError("current key has no name and parent is an objectnode");
            }
            ((ObjectNode) parent).set(pa.get(index).name(), v);
        } else {
            int size = pa.get(index).getIndexPosition() + 1;
            if (parent.size() < size) {
                for (int i = 0; i < size; i++) {
                    ((ArrayNode) parent).add(JsonNodeFactory.instance.nullNode());
                }
            }
            ((ArrayNode) parent).set(pa.get(index).getIndexPosition(), v);
        }
        return v;
    }

    public JsonNode getObjectNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        return JsonNodeFactory.instance.objectNode();
    }

    public JsonNode getArrayNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        return JsonNodeFactory.instance.arrayNode();
    }
}
