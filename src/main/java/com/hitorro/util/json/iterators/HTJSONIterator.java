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
package com.hitorro.util.json.iterators;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.json.*;
import com.hitorro.util.typesystem.TypeIntf;
import com.hitorro.util.typesystem.TypeManagerBase;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Iterator that wraps some kind of stream providing an collection of JSON domlets.
 * <p/>
 * You can provide a type name to insert at the top level a ht_type string.  This way we can lookup the type at runtime
 * from the object itself and do things like field defaulting.
 */
public class HTJSONIterator extends AbstractIterator<com.hitorro.util.json.JSONElement> {
    public static final com.hitorro.util.json.JSONMap EmptyMap = new com.hitorro.util.json.JSONMap(Collections.emptyMap());
    public static final com.hitorro.util.json.JSONList EmptyList = new com.hitorro.util.json.JSONList(Collections.emptyList());
    private final static JsonFactory factory = new JsonFactory();
    public static String HTTypeKey = "ht_type";
    private JsonParser parser = null;
    private com.hitorro.util.json.JSONElement domlet;

    private TreeMap<String, com.hitorro.util.json.JSONElement> tmpMap = new TreeMap();
    private List<com.hitorro.util.json.JSONElement> tmpList = new ArrayList();
    private TypeIntf type;

    public HTJSONIterator(Reader reader) {
        try {
            parser = factory.createJsonParser(reader);
        } catch (IOException e) {
            Log.filesystem.error("Unable to create parser %s %e", e, e);
        }
        domlet = read();
    }

    public HTJSONIterator(Reader reader, String typeName) {
        type = TypeManagerBase.get().getTypeByShortName(typeName);
        try {
            parser = factory.createJsonParser(reader);
        } catch (IOException e) {
            Log.filesystem.error("Unable to create parser %s %e", e, e);
        }
        domlet = read();
    }

    @Override
    public void close() throws Exception {
        if (parser != null) {
            parser.close();
        }
    }

    @Override
    public boolean hasNext() {
        return domlet != null;
    }

    @Override
    public com.hitorro.util.json.JSONElement next() {
        com.hitorro.util.json.JSONElement dom = domlet;
        domlet = read();
        if (type != null) {
            dom.setType(type);
        }
        return dom;
    }

    @Override
    public void remove() {

    }

    com.hitorro.util.json.JSONElement read() {
        try {
            JsonToken token = parser.nextToken();
            if (token == null || JsonToken.NOT_AVAILABLE == token) {
                return null;
            }
            return readPrivate();
        } catch (IOException e) {
            return null;
        }

    }

    private com.hitorro.util.json.JSONElement readPrivate() throws IOException {
        JsonToken tok = parser.getCurrentToken();
        switch (tok) {
            case START_OBJECT:
                return readObject();
            case START_ARRAY:
                return readArray();
            case VALUE_STRING:
                return new com.hitorro.util.json.JSONString(parser.getText());
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                return new com.hitorro.util.json.JSONNumber(parser.getNumberValue());
            case VALUE_TRUE:
                return com.hitorro.util.json.JSONBoolean.True;
            case VALUE_FALSE:
                return com.hitorro.util.json.JSONBoolean.False;
            case VALUE_NULL:
                return com.hitorro.util.json.JSONNull.me;
            default:


        }
        return null;
    }

    private com.hitorro.util.json.JSONElement readObject() throws IOException {
        parser.nextToken();
        TreeMap<String, com.hitorro.util.json.JSONElement> map = getNewMap();

        if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            // not an empty object
            do {
                String name = parser.getCurrentName();
                parser.nextToken();
                map.put(name, readPrivate());
                parser.nextToken();

            }
            while (parser.getCurrentToken() != JsonToken.END_OBJECT);
        }
        if (map.size() == 0) {
            // we didnt use the apply
            tmpMap = map;
            return EmptyMap;
        }
        return new com.hitorro.util.json.JSONMap(map);
    }

    private com.hitorro.util.json.JSONElement readArray() throws IOException {
        parser.nextToken();
        List<com.hitorro.util.json.JSONElement> list = getNewList();
        if (parser.getCurrentToken() != JsonToken.END_ARRAY) {
            // not an empty array
            do {
                list.add(readPrivate());
                parser.nextToken();
            }
            while (parser.getCurrentToken() != JsonToken.END_ARRAY);
        }
        if (ListUtil.nullOrEmpty(list)) {
            // we didnt use this guy,
            tmpList = list;
            return EmptyList;
        }
        return new com.hitorro.util.json.JSONList(list);
    }

    private TreeMap<String, com.hitorro.util.json.JSONElement> getNewMap() {
        if (tmpMap != null) {
            TreeMap<String, com.hitorro.util.json.JSONElement> tmp = tmpMap;
            tmpMap = null;
            return tmp;
        }
        return new TreeMap();
    }

    private List<com.hitorro.util.json.JSONElement> getNewList() {
        if (tmpList != null) {
            List<com.hitorro.util.json.JSONElement> tmp = tmpList;
            tmpList = null;
            return tmp;
        }
        return new ArrayList();
    }
}
