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
package com.hitorro.util.io.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JsonValueSource;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.typesystem.TypeFieldDataType;
import com.hitorro.util.typesystem.TypeFieldIntf;
import com.hitorro.util.typesystem.TypeIntf;

/**
 * Iterator that reads a csv file and outputs a json object that is a flat apply of fields. <<NULL>> is interpreted as
 * null and not empty string
 */
public class CSV2JSONIterator extends AbstractIterator<JsonValueSource> {
    public static final String NullElement = "<<NULL>>";

    private CSVIterator csvIter;
    private String header[];
    private TypeIntf type;
    private TypeFieldDataType types[];

    public CSV2JSONIterator(CSVIterator iter, TypeIntf type) {
        csvIter = iter;
        header = csvIter.getColumnNames();
        this.type = type;
        types = new TypeFieldDataType[header.length];
        if (type == null) {
            for (int i = 0; i < types.length; i++) {
                types[i] = TypeFieldDataType.String;
            }
        } else {
            for (int i = 0; i < types.length; i++) {
                TypeFieldIntf tfi = type.getField(header[i]);
                if (tfi == null) {
                    types[i] = TypeFieldDataType.String;
                } else {
                    types[i] = TypeFieldDataType.getFromClass(tfi.getImplementingClass());
                }
            }
        }

    }

    public CSV2JSONIterator(CSVIterator iter) {
        this(iter, null);
    }

    @Override
    public void close() throws Exception {
        csvIter.close();
    }

    @Override
    public boolean hasNext() {
        return csvIter.hasNext();
    }

    @Override
    public JsonValueSource next() {
        return map2Json();
    }

    @Override
    public void remove() {

    }

    private JsonValueSource map2Json() {
        if (csvIter.hasNext()) {
            String vals[] = csvIter.next();

            ObjectNode jMap = JsonNodeFactory.instance.objectNode();
            JsonValueSource jvs = new JsonValueSource(jMap);
            if (type != null) {
                jvs.setType(type);
            }
            for (int i = 0; i < Math.min(header.length, vals.length); i++) {
                if (StringUtil.nullOrEmptyString(header[i])) {
                    continue;
                }
                if (vals[i] == null || vals[i].equals(NullElement)) {
                    jMap.put(header[i], JsonNodeFactory.instance.nullNode());
                } else {
                    JsonNode elem = null;
                    switch (types[i]) {
                        case Int:
                            elem = JsonNodeFactory.instance.numberNode((Integer) types[i].convertFromString(vals[i]));
                            break;
                        case Long:
                            elem = JsonNodeFactory.instance.numberNode((Long) types[i].convertFromString(vals[i]));
                            break;
                        case Double:
                            elem = JsonNodeFactory.instance.numberNode((Double) types[i].convertFromString(vals[i]));
                            break;
                        case Float:
                            elem = JsonNodeFactory.instance.numberNode((Float) types[i].convertFromString(vals[i]));
                            break;
                        case Short:
                            elem = JsonNodeFactory.instance.numberNode((Short) types[i].convertFromString(vals[i]));
                            break;
                        case String:
                            String s = (String) types[i].convertFromString(vals[i]);
                            elem = JsonNodeFactory.instance.textNode(s);
                            break;
                    }
                    jMap.put(header[i], elem);
                }
            }


            return jvs;
        }
        return null;
    }
}
