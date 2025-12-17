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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;

//ht.jsontypesystem.dynamic.MultiValueMergerDM
public class MultiValueMergerDM extends DynamicFieldMapper {
    public static final StringProperty separatorKey = new StringProperty("seperator", "", ":");
    public static final StringProperty nullValueKey = new StringProperty("null", "", "null");

    private String nullValue;
    private String seperator;

    public boolean init(JsonNode node) {
        boolean flag = super.init(node);
        nullValue = nullValueKey.apply(node);
        seperator = separatorKey.apply(node);
        return flag;
    }

    @Override
    public JsonNode map(final JVS jvs, final Propaccess pa, final int depth) {
        StringBuilder sb = new StringBuilder();
        JsonNode arr[] = getValues(jvs, pa, depth);
        for (int i = 0; i < arr.length; i++) {
            if (sb.length() != 0) {
                sb.append(seperator);
            }
            if (arr[i] == null) {
                sb.append(nullValue);
            } else {
                sb.append(arr[i].textValue());
            }
        }
        return JsonNodeFactory.instance.textNode(sb.toString());
    }
}
