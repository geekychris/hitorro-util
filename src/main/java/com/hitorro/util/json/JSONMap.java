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
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.iterators.HTJSONIterator;
import com.hitorro.util.json.visitors.JSONVisitor;
import com.hitorro.util.typesystem.TypeFieldIntf;
import com.hitorro.util.typesystem.TypeIntf;
import com.hitorro.util.typesystem.TypeManagerBase;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class JSONMap extends JSONElement<Map<String, JSONElement>> implements Map<String, JSONElement> {
    private Map<String, JSONElement> map;
    private TypeIntf httype;

    public JSONMap() {
        this.map = new HashMap();
    }

    public JSONMap(Map<String, JSONElement> map) {
        this.map = map;
    }

    public String toString() {
        return Fmt.S("%m", map);
    }

    public Set<Map.Entry<String, JSONElement>> getSet() {
        return map.entrySet();
    }

    @Override
    public Map<String, JSONElement> get() {
        return map;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.Map;
    }

    @Override
    public void visit(final JSONVisitor visitor, int depth) {
    }

    public JSONElement put(String name, JSONElement elem) {
        return map.put(name, elem);
    }

    @Override
    public JSONElement remove(final Object o) {
        return map.remove(o);
    }

    public int getAggregateSize() {
        int counter = 0;
        Set<Map.Entry<String, JSONElement>> set = getSet();
        for (Map.Entry<String, JSONElement> e : set) {
            counter += e.getKey().length() * 2;
            counter += e.getValue().getAggregateSize();
        }
        return counter + 10;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        Set<Map.Entry<String, JSONElement>> set = getSet();
        for (Map.Entry<String, JSONElement> e : set) {
            JSONElement curr = e.getValue();

            if (curr.getJSONType() == JSONType.Map) {
                generator.writeObjectFieldStart(e.getKey());
                curr.write(generator);
                generator.writeEndObject();
            } else {
                generator.writeFieldName(e.getKey());
                curr.write(generator);
            }

        }
    }

    /**
     * get a child and if we have not completed the path traversal pass on the traversal to the child If you
     * inadvertently call a.b.c where b is a terminal node, such as a string b will return null.
     *
     * @param path
     * @param pos
     * @return
     */
    public JSONElement getFromPath(String path[], int pos, int maxLength, boolean addIfAbsent) {
        JSONElement elem = map.get(path[pos]);
        if (elem == null && addIfAbsent) {
            elem = new JSONMap(new TreeMap());
            map.put(path[pos], elem);
        }
        if (pos < maxLength - 1 && elem != null) {
            return elem.getFromPath(path, pos + 1, path.length);
        }
        return elem;
    }

    public TypeIntf getType() {
        if (httype != null) {
            return httype;
        }
        JSONElement type = map.get(HTJSONIterator.HTTypeKey);
        if (type != null) {
            httype = TypeManagerBase.get().getTypeByShortName(type.get().toString());
        }
        return null;
    }

    public void setType(TypeIntf type) {
        this.httype = type;
    }

    public JSONElement getFromPathDefaulting(String pathKey, String path[], int pos) {
        JSONElement e = getFromPath(path, pos, path.length);
        if (e != null) {
            return e;
        }
        TypeIntf typeI = getType();
        if (typeI != null) {
            TypeFieldIntf tfi = typeI.getField(pathKey);
            if (tfi == null) {
                return null;
            }
            ValueMapMapper vmm = tfi.getValueMapMapper();
            if (vmm == null) {
                return null;
            }
            vmm.compute(pathKey, this);
            return getFromPath(path, pos, path.length);
        }
        return null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(final Object o) {
        return containsKey(o);
    }

    @Override
    public boolean containsValue(final Object o) {
        return map.containsValue(o);
    }

    @Override
    public JSONElement get(final Object o) {
        return map.get(o);
    }

    @Override
    public void putAll(final Map map) {
        map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set keySet() {
        return map.keySet();
    }

    @Override
    public Collection values() {
        return map.values();
    }

    @Override
    public Set entrySet() {
        return map.entrySet();
    }

    public Object getValue(Object obj, String fieldName) {
        JSONElement elem = (JSONElement) obj;
        return elem.getFromPath(fieldName);
    }


    public void setValue(Object obj, String fieldName, Object value) {
        setValue(obj, fieldName, value, false);
    }

    public void setValue(Object obj, String fieldName, Object value, boolean ignoreTypeCheck) {
        String parts[] = StringUtil.tokenizeFromSingleChar(fieldName, ".");
        JSONElement pObject = this.getFromPathParent(parts, ignoreTypeCheck);
        if (pObject == null) {
            return;
        }
        if (!(pObject instanceof JSONMap)) {
            Log.util.error("field %s does not have a parent that is amp %s", fieldName);
            return;
        }
        JSONMap parent = (JSONMap) pObject;

        // if we are setting it, we must have the field type???
        TypeIntf intf = getType();

        if (intf == null) {
            Log.util.error("Unable to find type, not going to attempt set");
            return;
        }
        TypeFieldIntf tfi = intf.getField(fieldName);
        Class clazz;
        if (tfi == null) {
            if (ignoreTypeCheck) {
                clazz = value.getClass();
            } else {
                Log.util.error("Unable to find field %s for type %s, not going to attempt set", fieldName, intf);
                return;
            }
        } else {
            clazz = tfi.getImplementingClass();
        }

        JSONElement setThis = getJSONElementFromRaw(value, clazz);
        if (setThis != null) {
            parent.put(parts[parts.length - 1], setThis);
        }
    }

    public String[] getFieldNames() {
        return map.keySet().toArray(new String[map.size()]);
    }


    public UiProperties getUiProperties(Object obj, String fieldName) {
        return null;
    }
}
