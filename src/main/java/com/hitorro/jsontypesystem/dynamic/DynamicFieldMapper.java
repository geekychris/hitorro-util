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
package com.hitorro.jsontypesystem.dynamic;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Log;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.CollectionProperty;
import com.hitorro.util.json.keys.PropaccesspMap;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.util.List;

/**
 * Created by chris on 3/8/16.
 */


public abstract class DynamicFieldMapper implements JsonInitable {
    public static final CollectionProperty<Propaccess> propsKey = new CollectionProperty("fields", "", null, PropaccesspMap.instance);
    private Propaccess fields[];

    public boolean init(JsonNode node) {
        List<Propaccess> p = propsKey.apply(node);
        if (p == null) {
            fields = new Propaccess[0];
        } else {
            fields = p.toArray(new Propaccess[p.size()]);
        }
        return true;
    }

    public abstract JsonNode map(JVS jvs, Propaccess pa, int depth);

    protected JsonNode[] getValues(JVS jvs, Propaccess pa, int depth) {
        JsonNode arr[] = new JsonNode[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Propaccess f = fields[i];
            if (f.isRelative()) {
                Propaccess fNew = pa.clone();
                fNew.pop();
                fNew.append(f);

                try {
                    arr[i] = jvs.get(fNew);
                } catch (PropaccessError propaccessError) {
                    // XXX swallow
                    Log.type.error("DynamicFieldMapper(2) %s %e", propaccessError, propaccessError);
                }
            } else {
                try {
                    arr[i] = jvs.get(f);
                } catch (PropaccessError propaccessError) {
                    // XXX swallow
                    Log.type.error("DynamicFieldMapper(2) %s %e", propaccessError, propaccessError);
                }
            }
        }
        return arr;
    }

}
