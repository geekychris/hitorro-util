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
package com.hitorro.jsontypesystem.executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.Field;
import com.hitorro.jsontypesystem.Group;
import com.hitorro.jsontypesystem.SolrFieldType;
import com.hitorro.jsontypesystem.SolrFieldTypes;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

public class IndexerAction implements ExecutorAction<ExecutionBuilder> {
    protected Group group;
    protected Field field;
    private SolrFieldType sft;
    private String method;

    public IndexerAction(final Field field, Group group, final Propaccess path) {
        this.group = group;
        this.field = field;
        SolrFieldTypes sfts = SolrFieldTypes.solrFieldTypeCache.get();
        method = group.getMethod();
        sft = sfts.get(method);
    }


    public void project(ProjectionContext pc, Propaccess path, final boolean isMulti, final String lang) {
        try {
            JsonNode val = pc.source.get(path);

            if (val != null && !val.isNull()) {
                pc.sb.setLength(0);
                path.getPathSansIndex(pc.sb);
                sft.get(pc.sb, lang, isMulti);
                String field = pc.sb.toString();
                ObjectNode on = (ObjectNode) pc.target.getJsonNode();
                JsonNode n = on.get(field);
                if (n == null) {
                    // First value — store the node directly (preserves type: text, long, etc.)
                    on.set(field, val);
                } else {
                    // Multi-valued — promote to array
                    ArrayNode arr;
                    if (n.isArray()) {
                        arr = (ArrayNode) n;
                    } else {
                        arr = JsonNodeFactory.instance.arrayNode();
                        arr.add(n);
                        on.set(field, arr);
                    }
                    arr.add(val);
                }
            }
        } catch (PropaccessError propaccessError) {
            Log.util.error("IndexerAction projection failed for path %s: %s",
                    path, propaccessError.getMessage());
        }
    }
}
