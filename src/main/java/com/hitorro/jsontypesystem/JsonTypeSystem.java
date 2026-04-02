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
import com.hitorro.jsontypesystem.schema.Name2SchemaJsonMapper;
import com.hitorro.jsontypesystem.schema.TypeSchemaRegistry;
import com.hitorro.util.basefile.Name2JsonMapper;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.StringUtil;

/**
 *
 */
public class JsonTypeSystem {

    /**
     * When true, the type system loads type configs from .schema.json files
     * (config/schemas/) instead of native .json files (config/types/).
     * Set via system property "hitorro.typesystem.useJsonSchema=true" or
     * by calling {@link #setUseJsonSchema(boolean)} before first type access.
     */
    public static boolean useJsonSchema =
            "true".equalsIgnoreCase(System.getProperty("hitorro.typesystem.useJsonSchema"));

    public static HashCache<String, JsonNode> jsonTypeConfig =
            new HashCache<>(0, true,
                    null, "typesconfig",
                    new Name2JsonMapper(Env.getBinConfigBaseFile().getChild("types"), "core"));

    private static HashCache<String, JsonNode> schemaTypeConfig = null;

    public static HashCache<String, Type> typeCache =
            new HashCache<>(0, true,
                    null, "types",
                    new Name2TypeMapper());

    private static JsonTypeSystem me = null;

    private volatile TypeSchemaRegistry schemaRegistry;

    public static JsonTypeSystem getMe() {
        if (me == null) {
            me = new JsonTypeSystem();
        }
        return me;
    }

    /**
     * Enable or disable JSON Schema-based type loading.
     * Must be called before any types are loaded to take effect.
     */
    public static void setUseJsonSchema(boolean enable) {
        useJsonSchema = enable;
        if (enable) {
            Log.util.info("Type system switched to JSON Schema loading mode");
        }
    }

    /**
     * Get the schema-based config cache, creating it on first access.
     */
    static HashCache<String, JsonNode> getSchemaTypeConfig() {
        if (schemaTypeConfig == null) {
            BaseFile schemasDir = Env.getBinConfigBaseFile().getChild("schemas");
            schemaTypeConfig = new HashCache<>(0, true,
                    null, "typesconfig-schema",
                    new Name2SchemaJsonMapper(schemasDir, "core"));
        }
        return schemaTypeConfig;
    }

    public TypeSchemaRegistry getSchemaRegistry() {
        if (schemaRegistry == null) {
            synchronized (this) {
                if (schemaRegistry == null) {
                    schemaRegistry = new TypeSchemaRegistry();
                }
            }
        }
        return schemaRegistry;
    }

    public Type getType(String name) {
        if (StringUtil.nullOrEmptyString(name)) {
            return null;
        }
        return typeCache.get(name);
    }
}


class Name2TypeMapper implements Mapper<String, Type> {
    public Type apply(String s) {
        String key = s.toLowerCase();
        JsonNode node = null;

        if (JsonTypeSystem.useJsonSchema) {
            node = JsonTypeSystem.getSchemaTypeConfig().get(key);
            if (node != null) {
                Log.util.debug("Type '%s' loaded from JSON Schema", key);
            } else {
                // Fall back to native config — schema file may not exist for this type
                node = JsonTypeSystem.jsonTypeConfig.get(key);
                if (node != null) {
                    Log.util.debug("Type '%s' not found in schemas, fell back to native config", key);
                }
            }
        } else {
            node = JsonTypeSystem.jsonTypeConfig.get(key);
        }

        if (node == null) {
            return null;
        }
        return JsonNode2TypeMapper.mapper.apply(node);
    }
}

class JsonNode2TypeMapper implements Mapper<JsonNode, Type> {
    static final JsonNode2TypeMapper mapper = new JsonNode2TypeMapper();

    public JsonNode2TypeMapper() {
    }

    @Override
    public Type apply(JsonNode n) {
        Type t = new Type();
        t.init(n);
        return t;
    }
}
