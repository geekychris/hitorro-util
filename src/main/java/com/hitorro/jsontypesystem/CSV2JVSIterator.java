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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

public class CSV2JVSIterator extends AbstractIterator<JVS> {
    public static final String NullElement = "<<NULL>>";

    private CSVIterator csvIter;
    private String header[];
    private Type type;
    private PrimitiveTypeEnum types[];

    public CSV2JVSIterator(CSVIterator iter, Type type) {
        csvIter = iter;
        header = csvIter.getColumnNames();
        this.type = type;
        types = new PrimitiveTypeEnum[header.length];
        if (type == null) {
            for (int i = 0; i < types.length; i++) {
                types[i] = PrimitiveTypeEnum.String;
            }
        } else {
            for (int i = 0; i < types.length; i++) {
                Field tfi = type.getField(header[i]);
                if (tfi == null) {
                    types[i] = PrimitiveTypeEnum.String;
                } else {
                    types[i] = PrimitiveTypeEnum.getFromClass(tfi.getImplementingClass());
                }
            }
        }

    }

    public CSV2JVSIterator(CSVIterator iter) {
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
    public JVS next() {
        return map2Json();
    }

    @Override
    public void remove() {

    }

    private JVS map2Json() {
        if (csvIter.hasNext()) {
            String vals[] = csvIter.next();

            ObjectNode jMap = JsonNodeFactory.instance.objectNode();
            JVS jvs = new JVS(jMap);
            if (type != null) {
                try {
                    jvs.setType(type);
                } catch (PropaccessError propaccessError) {
                    //
                }
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
