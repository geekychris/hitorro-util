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
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.PropertyException;

public class Propaccess {
    private Part[] parts;
    private int length = 0;
    private boolean relative = false;
    public Propaccess(Propaccess path) {
        this(path, path.length);
    }

    public Propaccess(Propaccess path, int length) {
        copyToDepth(path, length);
    }

    public Propaccess(String path) {
        computeParts(path);
    }

    public static JsonNode appendObject(VS jvs, JsonNode node, String path, PAContext context, JsonNode elem) throws PropaccessError {
        Propaccess pa = new Propaccess(path);
        JsonNode prev = pa.set(jvs, node, context, elem);
        return prev;
    }

    public static JsonNode get(VS jvs, JsonNode node, String path, PAContext context) throws PropaccessError {
        Propaccess pa = new Propaccess(path);
        return pa.get(jvs, node, context);
    }

    public static JsonNode set(VS jvs, JsonNode node, String path, PAContext context, JsonNode elem) throws PropaccessError {
        Propaccess pa = new Propaccess(path);
        JsonNode prev = pa.set(jvs, node, context, elem);
        return prev;
    }

    public Propaccess clone() {
        return new Propaccess(this);
    }

    public Propaccess replaceLeaf(String leaf) {
        pop();
        append(leaf);
        return this;
    }

    public Part getLast() {
        if (length > 0) {
            return parts[length - 1];
        }
        return null;
    }

    public Part pop() {
        if (length > 0) {
            return parts[--length];
        }
        return null;
    }

    public Part get(int index) {
        return parts[index];
    }

    public JsonNode set(VS jvs, JsonNode node, PAContext context, JsonNode elem) throws PropaccessError {
        return getSet(jvs, SetMode.Set, node, context, elem);
    }

    public boolean isRelative() {
        return relative;
    }

    public void setRelative() {
        relative = true;
    }

    public boolean getLastElementArray() {
        return parts[length - 1].isIndexed();
    }

    public JsonNode appendObject(VS jvs, JsonNode node, PAContext context, JsonNode elem) throws PropaccessError {
        return getSet(jvs, SetMode.Append, node, context, elem);
    }

    public JsonNode set(VS jvs, JsonNode node, PAContext context, JsonNode elem, int depth) throws PropaccessError {
        return getSet(jvs, SetMode.Set, node, context, elem, depth, false);
    }

    public JsonNode get(VS jvs, JsonNode node, PAContext context) throws PropaccessError {
        return getSet(jvs, SetMode.No, node, context, null);
    }

    public int getSize(VS jvs, JsonNode node, PAContext context) throws PropaccessError {
        return getSize(jvs, node, context, length);
    }

    protected JsonNode getSet(VS jvs, SetMode set, JsonNode nodeIn, PAContext context, JsonNode elem) throws PropaccessError {
        return getSet(jvs, set, nodeIn, context, elem, length, false);
    }

    protected boolean previousIsArray(int pos) {
        if (pos == 0) {
            return false;
        }
        return parts[pos - 1].isIndexed();
    }

    protected int getSize(VS jvs, JsonNode nodeIn, PAContext context, int depth) throws PropaccessError {
        // hack the getter to return the parent array.
        JsonNode node = getSet(jvs, SetMode.No, nodeIn, context, null, depth, true);
        if (node == null) {
            return 0;
        }
        return node.size();
    }

    /**
     * key[] maybe of the form key[int] or key[string].  In the case of a string we have to attempt to
     * resolve that to an index position assuming the compContext has a way to resolve it (such as through helpers in the type system)
     *
     * @param jvs
     * @param context
     * @param node
     * @param i
     * @param p
     * @return
     */
    private int getArrayNodeIndexPosConsideringVariables(final VS jvs, final PAContext context, final JsonNode node, final int i, final Part p) {
        if (p.isNull()) {
            return -1;
        }
        int indexPos;
        if (p.isNumeric()) {
            indexPos = p.getIndexPosition();
        } else {
            indexPos = context.translateIndexPos(jvs, (ArrayNode) node.get(p.name()), this, i);
            // have to compute it!
        }
        return indexPos;
    }

    private boolean doSet(final SetMode set, final int i, boolean retLeaf) {
        return (set == SetMode.Set || set == SetMode.Append || retLeaf) && i == length - 1;
    }

    public void computeParts(String path) throws PropertyException {
        if (path.length() == 0) {
            parts = new IndexedPart[0];
            length = 0;
            return;
        }
        if (!StringUtil.ensureBalance(path, '[', ']')) {
            throw new PropertyException("Unbalanced ");
        }
        int count = StringUtil.countInstances(path, '[');
        if (path.length() > 0 && path.charAt(0) == '.') {
            relative = true;
            path = path.substring(1, path.length());
        }
        String p[] = path.split("\\.");
        if (ArrayUtil.nullOrEmpty(p)) {
            parts = new IndexedPart[0];
            length = 0;
            return;
        }
        int total = p.length + count;
        parts = new Part[total];
        length = 0;
        for (int i = 0; i < p.length; i++) {
            int index = p[i].indexOf('[');
            if (index == -1) {
                parts[length++] = new Part(p[i]);
            } else {
                String s = p[i];
                IndexedPart ip = new IndexedPart(s.substring(0, index));
                ip.setValue(s.substring(index + 1, s.length() - 1));
                parts[length++] = ip;
            }
        }
    }

    public void setLength(int l) {
        length = l;
    }

    public Propaccess copy(int depth) {
        Propaccess path = new Propaccess(this, depth);
        return path;
    }

    protected JsonNode getSet(VS jvs, SetMode set, JsonNode nodeIn, PAContext context, JsonNode setElem, int depth, boolean retLeaf) throws PropaccessError {
        if (setElem != null && setElem.isNull()) {
            Console.println();
        }
        if (length == 0) {
            return nodeIn;
        }

        JsonNode node = nodeIn;
        length = Math.min(length, depth);
        for (int i = 0; i < length; i++) {
            if (node == null) {
                return null;
            }
            Part p = parts[i];
            if (p.isIndexed() || set == SetMode.Append && i == depth - 1) {
                int indexPos = getArrayNodeIndexPosConsideringVariables(jvs, context, node, i, p);

                /**
                 * current depth is key[] or [] format.
                 * parent is array:
                 *  de-ref the parents index position, if null put to that position an empty array.
                 *
                 */
                boolean isNewElem = false;
                if (true/*!previousIsArray(i)*/) {
                    /**
                     * Can only do this step if we are not a nested set of arrays.
                     */
                    JsonNode v;
                    if (node.isArray()) {
                        // array could exist but element maybe missing
                        v = node.get(indexPos);
                    } else {
                        v = node.get(p.name());
                    }
                    if (v == null) {
                        isNewElem = true;
                        node = context.getObjectOrArrayIfMissing(jvs, node, v,
                                this, i, false, set);
                    } else {
                        node = v;
                    }

                    if (node == null) {
                        return null;
                    }
                } else {
                    // XXX Nested set of arrays ...what do we do?
                }

                if (node.isArray()) {
                    if (doSet(set, i, retLeaf)) {
                        if (retLeaf) {
                            return node;
                        }
                        if (set == SetMode.Set) {
                            // return array not its element for setter processing
                            if (node.size() - 1 < indexPos) {
                                int s = node.size();
                                for (int j = s - 1; j < indexPos; j++) {
                                    ((ArrayNode) node).add(JsonNodeFactory.instance.nullNode());
                                }
                            }
                            return ((ArrayNode) node).set(indexPos, setElem);
                        }
                        return ((ArrayNode) node).add(setElem);
                    } else {
                        node = context.getObjectOrArrayIfMissing(jvs, node, node.get(indexPos),
                                this, i, true, set);
                    }
                    if (node == null) {
                        return null;
                    }
                }
            } else {
                if (!node.isObject()) {
                    throw new PropaccessError("object node expected %s %s", this, i);
                }

                if (doSet(set, i, retLeaf)) {
                    if (retLeaf) {
                        return node.get(p.name());
                    }
                    if (setElem == null) {
                        // remove the element
                        return ((ObjectNode) node).remove(p.name());
                    }
                    return ((ObjectNode) node).set(p.name(), setElem);
                } else {
                    // XXX is this really an alternative at the last node doing a set?  Is it possible that
                    // we need todo this step before the step
                    node = context.getObjectOrArrayIfMissing(jvs, node, node.get(p.name()), this, i, false, set);
                }
                if (node == null) {
                    return null;
                }
            }
        }
        return node;
    }

    private void copyToDepth(final Propaccess path, int length) {
        parts = new Part[length];
        for (int i = 0; i < length; i++) {
            parts[i] = path.parts[i].clone();
        }
        this.length = length;
    }

    public void append(String path) {
        append(new Propaccess(path));
    }

    public void append(Propaccess propa) {
        append(propa.parts, propa.length);
    }

    public void append(Part part) {
        parts = ArrayUtil.ensureCapacity(Part.class, length + 1, length, parts);
        parts[length] = part;
        length++;
    }

    public void append(Part partsIn[], int lengthIn) {
        parts = ArrayUtil.ensureCapacity(Part.class, length + lengthIn, length, parts);
        for (int i = 0; i < lengthIn; i++) {
            parts[length + i] = partsIn[i];
            length++;
        }

    }

    public int length() {
        return length;
    }

    public String toString() {
        return getPath();
    }

    public String getPath() {
        return getPath(new StringBuilder());
    }

    public String getPath(StringBuilder sb) {
        for (int i = 0; i < length; i++) {
            parts[i].append(sb);
        }
        return sb.toString();
    }

    public String getPathSansIndex() {
        StringBuilder sb = new StringBuilder();
        return getPathSansIndex(sb);
    }

    public String getPathSansIndex(StringBuilder sb) {
        for (int i = 0; i < length; i++) {
            if (i != 0) {
                sb.append('.');
            }
            sb.append(parts[i].name);
        }
        return sb.toString();
    }

    public enum SetMode {
        No, Set, Append
    }
}
