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
import com.hitorro.jsontypesystem.dynamic.DynamicFieldMapper;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.CollectionProperty;
import com.hitorro.util.json.keys.JsonInitableProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.typesystem.FieldBaseIntf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class Field extends BaseT implements FieldBaseIntf {
    public static final BooleanProperty i18nKey = new BooleanProperty("i18n", "", false);
    public static final BooleanProperty vectorKey = new BooleanProperty("vector", "", false);
    public static JsonInitableProperty<DynamicFieldMapper> dynamicFieldMapperKey = new JsonInitableProperty<>("dynamic", "", null, DynamicFieldMapper.class, null);
    public static JsonInitableProperty<Group> groupKey = new JsonInitableProperty<>("", "", null, Group.class, Group.class);
    public static CollectionProperty<Group> groupsKey = groupKey.collection("groups", "", new ArrayList<>());
    private Type type;
    private boolean vector;
    private Map<String, Collection<Group>> groups = new HashMap<>();

    private DynamicFieldMapper dynamicFieldMapper = null;
    private boolean i18n = false;

    public void visit(TypeVisitor visitor, Predicate<BaseT> filter, Propaccess path) {
        type.visit(visitor, filter, path);
        for (Collection<Group> gs : groups.values()) {
            for (Group g : gs) {
                if (filter.test(g)) {
                    visitor.enterGroup(this, g, path);
                    g.visit(visitor, filter, path);
                    visitor.leaveGroup(this, g, path);
                }
            }
        }
    }

    public boolean isI18n() {
        return i18n;
    }

    public Class getImplementingClass() {
        if (type.isPrimitiveType()) {
            type.getPrimitiveType().getSerializedClass();
        }
        return null;
    }

    @Override
    public boolean init(final JsonNode node) {
        boolean flag = super.init(node);
        this.type = JsonTypeSystem.getMe().getType(typeKey.apply(node));
        this.vector = vectorKey.apply(node);
        this.dynamicFieldMapper = dynamicFieldMapperKey.apply(node);
        this.i18n = i18nKey.apply(node);
        Collection<Group> g = groupsKey.apply(node);
        MapUtil.addToMapArray(groups, g, Group::getName);
        return true;
    }

    public Group getDefaultGroupFor(String groupName) {
        Collection<Group> gr = groups.get(groupName);
        if (gr != null) {
            Group first = null;
            for (Group g : gr) {
                if (first == null) {
                    first = g;
                }
                if (g.isDefault()) {
                    return g;
                }
            }
            return first;
        }
        return null;
    }

    public boolean isVector() {
        return vector;
    }

    public DynamicFieldMapper getDynamicFieldMapper() {
        return dynamicFieldMapper;
    }

    public boolean isDynamic() {
        return dynamicFieldMapper != null;
    }

    public String toString() {
        if (getType() != null) {
            return Fmt.S("Field: %s, type: %s", getName(), getType().getName());
        }
        return null;
    }

    public Type getType() {
        return type;
    }

    public Collection<Group> getGroup(String name) {
        return groups.get(name);
    }


}
