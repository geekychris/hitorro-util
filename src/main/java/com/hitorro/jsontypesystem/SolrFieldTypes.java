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
import com.hitorro.util.basefile.Name2JsonMapper;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.events.cache.HashCache;
import com.hitorro.util.core.events.cache.SingletonCache;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.keys.JsonInitableProperty;
import com.hitorro.util.json.keys.MapProperty;

import java.util.HashMap;
import java.util.Map;

public class SolrFieldTypes {
    public static HashCache<String, JsonNode> solrFieldTypesConfig =
            new HashCache<>(0, true,
                    null, "solrconfig",
                    new Name2JsonMapper(Env.getBinConfigBaseFile().getChild("jsonconfigs"), "solr"));

    public static SingletonCache<SolrFieldTypes> solrFieldTypeCache =
            new SingletonCache<SolrFieldTypes>(true,
                    true, "solffieldtypes",
                    new Name2SolrFieldTypesMapper(), null);

    public static JsonInitableProperty<SolrFieldType> SolrFieldTypeKey = new JsonInitableProperty("", "", null, SolrFieldType.class, SolrFieldType.class);

    public static MapProperty<String, SolrFieldType> SolrFields = SolrFieldTypeKey.mapProperty("fields", "", null, SolrFieldType.Name);

    protected Map<String, SolrFieldType> map = new HashMap();

    public SolrFieldType get(String name) {
        return map.get(name);
    }
}

class Name2SolrFieldTypesMapper implements Mapper<Object, SolrFieldTypes> {
    public SolrFieldTypes apply(Object s) {
        JsonNode node = SolrFieldTypes.solrFieldTypesConfig.get("solr_fields");
        if (node == null) {
            return null;
        }
        SolrFieldTypes sft = new SolrFieldTypes();
        sft.map = SolrFieldTypes.SolrFields.apply(node);
        return sft;
    }
}