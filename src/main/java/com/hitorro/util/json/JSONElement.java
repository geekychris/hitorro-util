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
package com.hitorro.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.visitors.JSONVisitor;
import com.hitorro.util.typesystem.TypeIntf;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

import java.io.IOException;

/**
 *
 */
public abstract class JSONElement<E> implements ValueSourceForClass {
    public static JSONElement getJSONElementFromRaw(final Object value, final Class clazz) {
        if (clazz.equals(String.class)) {
            return new JSONString(value.toString());
        } else if (clazz.equals(Long.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Float.class)) {
            return new JSONNumber((Number) value);
        } else {
            Log.util.error("Dont know how to apply type %s", clazz.getName());
        }
        return null;
    }

    public abstract E get();

    public abstract JSONType getJSONType();

    public abstract int getAggregateSize();

    public void visit(JSONVisitor visitor) {
        visit(visitor, 0);
    }

    /**
     * Called at the root object to encapsulate the whole json tree in a a start and end.
     *
     * @param generator
     * @throws IOException
     */
    public void writeJSONGraph(JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        write(generator);
        generator.writeEndObject();
        generator.writeRawValue(Constants.NewLineString);
    }

    public abstract void write(JsonGenerator generator) throws IOException;

    public abstract void visit(JSONVisitor visitor, int level);

    public JSONElement getFromPath(String path) {
        return getFromPath(path, false);
    }

    public JSONElement getFromPath(String path, boolean createIfMissing) {
        String parts[] = StringUtil.tokenizeFromSingleChar(path, ".");
        return getFromPath(parts, 0, parts.length, createIfMissing);
    }

    public JSONElement getFromPathParent(String parts[]) {
        return getFromPathParent(parts, false);
    }

    public JSONElement getFromPathParent(String parts[], boolean addIfAbsent) {

        if (parts.length == 1) {
            // we are already there.
            return this;
        }
        return getFromPath(parts, 0, parts.length - 1, addIfAbsent);
    }

    public JSONElement getFromPathDefaulting(String pathKey) {
        return getFromPathDefaulting(pathKey, StringUtil.tokenizeFromSingleChar(pathKey, ".", false), 0);
    }

    /**
     * gets an element using a dotted notation.  This currently does not know how to handle vectors. In the case of
     * traversing the vector you would want to get the vector and iterate over that using listIterator
     *
     * @param path
     * @param pos
     * @return
     */
    public JSONElement getFromPath(String path[], int pos, int maxLength) {
        return getFromPath(path, pos, maxLength, false);
    }

    public JSONElement getFromPath(String path[], int pos, int maxLength, boolean addIfAbsent) {
        return null;
    }

    public JSONElement getFromPathDefaulting(String pathKey, String path[], int pos) {
        return null;
    }

    public Object getValue(Object obj, String fieldName) {
        JSONElement elem = (JSONElement) obj;
        return elem.getFromPath(fieldName);
    }

    public void setValue(Object obj, String fieldName, Object value) {

    }

    public void setValue(Object obj, String fieldName, Object value, boolean ignoreTypeCheck) {

    }

    public String[] getFieldNames() {
        return null;
    }

    public TypeIntf getType() {
        return null;
    }

    public void setType(TypeIntf type) {
    }

    public UiProperties getUiProperties(Object obj, String fieldName) {
        return null;
    }

    @Override
    public Object getValue(final String fieldName) {
        return this.getFromPath(fieldName);
    }

}
