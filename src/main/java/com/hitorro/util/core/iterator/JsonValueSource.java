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
package com.hitorro.util.core.iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.params.PropertiesUtil;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.PropertyParts;
import com.hitorro.util.typesystem.TypeFieldIntf;
import com.hitorro.util.typesystem.TypeIntf;
import com.hitorro.util.typesystem.annotation.UiProperties;
import com.hitorro.util.typesystem.valuesource.ValueMapMapper;
import com.hitorro.util.typesystem.valuesource.ValueSourceForClass;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JsonValueSource implements ValueSourceForClass {
    public static String TypeField = "ht_type";
    private JsonNode node;
    private TypeIntf type;
    private PropertyParts pp = new PropertyParts("a");

    public JsonValueSource(JsonNode node) {
        this.node = node;
    }

    public JsonValueSource() {
        this.node = JsonNodeFactory.instance.objectNode();
    }

    public JsonNode getNode() {
        return node;
    }

    public TypeIntf getType() {
        if (type != null) {
            return type;
        }
        return null;
    }

    public void setType(TypeIntf type) {
        setValue(this, TypeField, type.getName());
        this.type = type;
    }

    public Object getValue(Object obj, String fieldName) {
        return getValue(fieldName);
    }

    /**
     * Attempt to get a value from the apply, if not found, attempt to fill in if its a dynamic field
     *
     * @param fieldName
     * @return
     */
    public Object getValue(String fieldName) {
        pp.setPath(fieldName);
        Object o = PropertiesUtil.getNode(pp, node, false);
        if (o != null) {
            return o;
        }
        TypeIntf typeI = getType();
        if (typeI != null) {
            TypeFieldIntf tfi = typeI.getField(fieldName);
            if (tfi == null) {
                return null;
            }
            ValueMapMapper vmm = tfi.getValueMapMapper();
            if (vmm == null) {
                return null;
            }
            vmm.compute(fieldName, this);
            return PropertiesUtil.getNode(pp, node, false);
        }
        return null;
    }

    public void setValue(Object ojb, String fieldName, Object value) {
        setValue(ojb, fieldName, value, false);
    }

    public void setValue(Object obj, String fieldName, Integer integer) {
        setValue(obj, fieldName, JsonNodeFactory.instance.numberNode(integer));
    }

    /**
     * a.b[1] - your assuming a certainly exists, maybe b and you will want to set the nth position a.b - doesn't matter
     * about b, we are assuming b is a property of a where a is a apply.
     *
     * @param obj
     * @param fieldName
     * @param value
     */
    public void setValue(Object obj, String fieldName, Object value, boolean ignoreTypeCheck) {
        // XXX TODO do something with type checking!!!!
        if (value == null) {
            return;
        }
        pp.setPath(fieldName);
        if (pp.lastPartIndexed()) {
            ArrayNode an = PropertiesUtil.getNodeAsArrayCreating(pp, node, true);
            int index = pp.getIndex();
            //do we need to expand?
            while (an.size() <= index) {
                an.addNull();
            }
            an.set(index, JSONUtil.ensureJsonNode(value));
        } else {
            // not indexed
            JsonNode par = PropertiesUtil.getNodeParent(pp, node, true);
            if (par.isObject()) {
                ((ObjectNode) par).put(pp.getName(), JSONUtil.ensureJsonNode(value));
            }
        }
        // throw error?
    }


    public String[] getFieldNames() {
        List<String> n = new ArrayList();
        node.size();
        //n.addAll(node.getFieldNames());
        return null;
    }

    public UiProperties getUiProperties(Object obj, String fieldName) {
        return null;
    }
}
