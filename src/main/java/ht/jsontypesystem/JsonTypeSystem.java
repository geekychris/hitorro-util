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
package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.Name2JsonMapper;
import ht.util.core.Env;
import ht.util.core.events.cache.HashCache;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.StringUtil;


/**
 *
 */
public class JsonTypeSystem {

    public static HashCache<String, JsonNode> jsonTypeConfig =
            new HashCache<>(0, true,
                    null, "typesconfig",
                    new Name2JsonMapper(Env.getBinConfigBaseFile().getChild("types"), "core"));

    public static HashCache<String, Type> typeCache =
            new HashCache<>(0, true,
                    null, "types",
                    new Name2TypeMapper());

    private static JsonTypeSystem me = null;

    public static JsonTypeSystem getMe() {
        if (me == null) {
            me = new JsonTypeSystem();
        }
        return me;
    }

    public Type getType(String name) {
        if (StringUtil.nullOrEmptyString(name)) {
            return null;
        }
        if (StringUtil.nullOrEmptyString(name)) {
            return null;
        }
        return typeCache.get(name);
    }
}


class Name2TypeMapper implements Mapper<String, Type> {
    public Type apply(String s) {
        JsonNode node = JsonTypeSystem.jsonTypeConfig.get(s.toLowerCase());
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