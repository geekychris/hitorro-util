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
import com.hitorro.jsontypesystem.executors.EnrichExecutionBuilderMapper;
import com.hitorro.jsontypesystem.executors.ExecutionBuilder;
import com.hitorro.jsontypesystem.executors.IndexExecutionBuilderMapper;
import com.hitorro.jsontypesystem.executors.RemoveExecutionBuilderMapper;
import com.hitorro.util.core.classes.JsonNode2JsonInitable;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.*;
import com.hitorro.util.json.keys.propaccess.PAContext;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.typesystem.TypeBaseIntf;
import com.hitorro.util.typesystem.TypeFieldDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 */
public class Type extends BaseT implements TypeBaseIntf {
    public static final com.hitorro.util.json.keys.BooleanProperty fetchLangKey = new com.hitorro.util.json.keys.BooleanProperty("fetchlang", "", false);
    public static HashCache<String, HashCache<Type, ExecutionBuilder>> projectionCache = new HashCache<>(1000000
            , true, null, "execution builders", ExecutionBuilderCacheMapper.ebcm);
    public static com.hitorro.util.json.keys.StringProperty supertypeKey = new com.hitorro.util.json.keys.StringProperty("super", "object super type", null);
    public static JsonNode2JsonInitable<Field> fieldKey = new JsonNode2JsonInitable<>(Field.class, "class", Field.class);
    public static com.hitorro.util.json.keys.MapProperty<String, Field> fieldsKey =
            new com.hitorro.util.json.keys.MapProperty<>("fields", "", new HashMap<>(), nameKey, fieldKey);
    public static com.hitorro.util.json.keys.JsonInitableProperty<IndexSeeker> indexSeekerKey = new com.hitorro.util.json.keys.JsonInitableProperty<>("indexseeker", "", null, IndexSeeker.class, null);
    private static com.hitorro.util.json.keys.EnumProperty<com.hitorro.util.typesystem.TypeFieldDataType> primitiveTypeKey =
            new com.hitorro.util.json.keys.EnumProperty<>("primitivetype", "", null, TypeFieldDataType.fieldDataType);

    static {
        ExecutionBuilderCacheMapper.ebcm.add("index", IndexExecutionBuilderMapper.me);
        ExecutionBuilderCacheMapper.ebcm.add("enrich", EnrichExecutionBuilderMapper.me);
        ExecutionBuilderCacheMapper.ebcm.add("remove", RemoveExecutionBuilderMapper.me);
    }

    private Type superType;
    private TypeFieldDataType primitiveType;
    private JsonNode node;
    private Map<String, Field> fields = new HashMap<>();
    private boolean fetchLang;

    private IndexSeeker indexSeeker;

    public static HashCache<Type, ExecutionBuilder> getExecBuilderCache(String name, BaseMapper<Type, ExecutionBuilder> mapper) {
        return new HashCache<Type, ExecutionBuilder>(1000000, true, null, name, mapper);
    }

    public String toString() {
        return Fmt.S("Type: %s", getName());
    }

    public Group getDefaultGroupFor(Propaccess access, String groupName) {
        Type t = this;
        Field f = null;
        for (int i = 0; i < access.length(); i++) {
            f = t.getField(access.get(i).name());
            if (f != null) {

                t = f.getType();
            } else {
                return null;
            }
        }
        if (f != null) {
            return f.getDefaultGroupFor(groupName);
        }
        return null;
    }

    public boolean isMultiValuedPath(Propaccess access) {
        Type t = this;
        for (int i = 0; i < access.length(); i++) {
            Field f = t.getField(access.get(i).name());
            if (f != null) {
                if (f.isVector()) {
                    return true;
                }
                t = f.getType();
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * Determine the common path length between path and fields of the type.
     *
     * @param access
     * @return
     */
    public int getMaxCommonDepth(Propaccess access) {
        Type t = this;
        for (int i = 0; i < access.length(); i++) {
            Field f = t.getField(access.get(i).name());
            if (f != null) {
                t = f.getType();
            } else {
                return i;
            }
        }
        return access.length();
    }

    public void visit(TypeVisitor visitor, Predicate<BaseT> filter, Propaccess path) {
        visitor.enterType(this, path);
        for (Field f : fields.values()) {
            path.append(f.getName());
            if (visitor.enterField(f, path)) {
                f.visit(visitor, filter, path);
            }
            visitor.leaveField(f, path);
            path.pop();
        }
        visitor.leaveType(this, path);
    }

    @Override
    public boolean init(final JsonNode node) {
        boolean flag = super.init(node);
        this.node = node;
        superType = JsonTypeSystem.getMe().getType(supertypeKey.apply(node));

        fields = fieldsKey.apply(node);
        if (superType != null) {
            fields = MapUtil.mergeMaps(superType.fields, fields);
        }

        primitiveType = primitiveTypeKey.apply(node);
        if (primitiveType == null && superType != null) {
            primitiveType = superType.primitiveType;
        }
        indexSeeker = indexSeekerKey.apply(node);
        if (indexSeeker == null && superType != null) {
            indexSeeker = superType.indexSeeker;
        }
        fetchLang = fetchLangKey.apply(node);

        return flag;
    }

    public boolean fetchLang() {
        return fetchLang;
    }

    public IndexSeeker getIndexSeeker() {
        return indexSeeker;
    }

    public boolean isPrimitiveType() {
        return primitiveType != null;
    }

    public TypeFieldDataType getPrimitiveType() {
        return primitiveType;
    }

    public Type getSuper() {
        return superType;
    }

    public Field getField(String name) {
        Field f = fields.get(name.toLowerCase());
        if (f == null && superType != null) {
            return superType.getField(name);
        }
        return f;
    }

    public Field getField(Propaccess access) {
        return getField(access, 0, access.length() - 1);
    }

    public Field getField(Propaccess access, int maxDepth) {
        return getField(access, 0, Math.min(access.length() - 1, maxDepth));
    }

    private Field getField(Propaccess access, int depth, int maxDepth) {
        //XXX how do we model [][]?

        Field f = getField(access.get(depth).name());
        if (f != null && depth < maxDepth) {
            return f.getType().getField(access, depth + 1, maxDepth);
        }
        return f;
    }

    public PAContext getPaContext() {
        //XXX replace with one that uses this type
        return new PAContextTyped(this);
    }
}


class ExecutionBuilderCacheMapper extends BaseMapper<String, HashCache<Type, ExecutionBuilder>> {
    public static ExecutionBuilderCacheMapper ebcm = new ExecutionBuilderCacheMapper();
    private Map<String, BaseMapper<Type, ExecutionBuilder>> factories = new HashMap<>();

    public void add(String key, BaseMapper<Type, ExecutionBuilder> mapper) {
        factories.put(key, mapper);
    }

    @Override
    public HashCache<Type, ExecutionBuilder> apply(final String s) {
        BaseMapper<Type, ExecutionBuilder> mapper = factories.get(s);
        if (mapper == null) {
            return null;
        }

        return getCache(s, mapper);
    }

    private HashCache<Type, ExecutionBuilder> getCache(final String s, final BaseMapper<Type, ExecutionBuilder> mapper) {
        return Type.getExecBuilderCache(s, mapper);
    }
}

