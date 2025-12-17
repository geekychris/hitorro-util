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
package com.hitorro.util.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Keep track of objects and a numeric id.
 */
public class IDObjectMap<E> {
    private int counter = 0;
    private TObjectIntHashMap<E> oIntMap = new TObjectIntHashMap<E>();
    private TIntObjectHashMap<E> intOMap = new TIntObjectHashMap<E>();
    private TObjectIntHashMap<String> nameIdMap = new TObjectIntHashMap<String>();

    public final int getOrAdd(E e, String name) {
        if (oIntMap.contains(e)) {
            return oIntMap.get(e);
        }
        int i = counter++;
        oIntMap.put(e, i);
        intOMap.put(i, e);
        nameIdMap.put(name, i);
        return i;
    }

    public final E getObject(int id) {
        return intOMap.get(id);
    }

    public JsonNode getMap() {
        ObjectNode s2i = JsonNodeFactory.instance.objectNode();

        ObjectNode i2s = JsonNodeFactory.instance.objectNode();
        TObjectIntIterator<String> iter = nameIdMap.iterator();
        while (iter.hasNext()) {
            iter.advance();
            String key = iter.key();
            int val = iter.value();
            ObjectNode s2iRow = JsonNodeFactory.instance.objectNode();
            s2iRow.put(key, val);

            i2s.put(Integer.toString(val), key);

        }
        ObjectNode ret = JsonNodeFactory.instance.objectNode();
        ret.set("s2i", s2i);
        ret.set("i2s", i2s);
        return ret;
    }

    public final int getObjectIdByName(String name) {
        if (!nameIdMap.contains(name)) {
            return -1;
        }
        return nameIdMap.get(name);
    }

}
