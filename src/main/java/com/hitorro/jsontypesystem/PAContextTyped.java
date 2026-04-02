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
import com.hitorro.jsontypesystem.dynamic.DynamicFieldMapper;
import com.hitorro.util.json.keys.propaccess.PAContext;
import com.hitorro.util.json.keys.propaccess.Part;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.json.keys.propaccess.VS;

public class PAContextTyped extends PAContext {
    private Type type;

    public PAContextTyped(Type type) {
        this.type = type;
    }

    protected boolean attemptCreation(final JsonNode node, final Propaccess.SetMode setMode) {
        return (node == null || node.isNull());
    }

    @Override
    public JsonNode getObjectOrArrayIfMissing(VS jvs, JsonNode parent, JsonNode node,
                                              Propaccess pa, int index, boolean secondStage, Propaccess.SetMode setMode) throws PropaccessError {
        // Check if this is an indexed field with a non-numeric index (like a language code)
        // This is called with secondStage=true when traversing into an array element (e.g., mls[en].text)
        if (attemptCreation(node, setMode) && parent != null && parent.isArray()) {
            Part part = pa.get(index);
            if (part.isIndexed() && !part.isNumeric()) {
                // This is a language-style indexed lookup that didn't find an existing element
                Field f = type.getField(pa, index);
                if (f != null) {
                    IndexSeeker is = f.getType().getIndexSeeker();
                    if (is != null) {
                        // Create element using IndexSeeker (e.g., {"lang": "fr"})
                        JsonNode newElement = is.createElement(part.getIndexAsValue());
                        if (newElement != null) {
                            // Append to parent array and return the new element
                            ((ArrayNode) parent).add(newElement);
                            return newElement;
                        }
                    }
                }
            }
        }
        return super.getObjectOrArrayIfMissing(jvs, parent, node, pa, index, secondStage, setMode);
    }

    public int translateIndexPos(VS jvs, ArrayNode node, Propaccess pa, int depth) {
        String indexValue = pa.get(depth).getIndexAsValue();
        if (indexValue == null || indexValue.isEmpty()) {
            return -1;
        }
        Field f = type.getField(pa, depth);
        if (f != null) {
            IndexSeeker is = f.getType().getIndexSeeker();
            if (is != null) {
                return is.getIndex(node, pa, depth, indexValue, (JVS) jvs);
            }
        }
        return -1;
    }

    public JsonNode getObjectNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        if (setMode == Propaccess.SetMode.Set || setMode == Propaccess.SetMode.Append) {
            return super.getObjectNode(jvs, pa, index, setMode);
        }
        return getEither(jvs, pa, index, setMode);
    }

    public JsonNode getArrayNode(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        if (setMode == Propaccess.SetMode.Set || setMode == Propaccess.SetMode.Append) {
            return super.getArrayNode(jvs, pa, index, setMode);
        }
        return getEither(jvs, pa, index, setMode);
    }

    public JsonNode getEither(VS jvs, Propaccess pa, int index, Propaccess.SetMode setMode) {
        Field f = type.getField(pa, index);

        if (f != null) {
            if (f.isDynamic()) {
                DynamicFieldMapper dfm = f.getDynamicFieldMapper();
                return dfm.map((JVS) jvs, pa, index);
            }
        }

        return null;
    }
}
