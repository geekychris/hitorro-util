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
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;

//
public class SolrFieldType implements JsonInitable {
    public static BooleanProperty Is18N = new BooleanProperty("i18n", "", false);
    public static BooleanProperty IsId = new BooleanProperty("isid", "", false);
    public static StringProperty Name = new StringProperty("name", "", null);

    private boolean is_18n;
    private boolean isId;
    private String indexType;

    public String toString() {
        return Fmt.S("SFT: i18n:%s isid: %s type:%s", Boolean.toString(is_18n), Boolean.toString(isId), indexType);
    }

    public void get(StringBuilder fieldPath, String lang, boolean isMulti) {
        if (is_18n) {
            // this.is.my.path.index_type_lang_m
            Fmt.f(fieldPath, ".%s_%s_%s", indexType, lang, getMulti(isMulti));
        } else {
            Fmt.f(fieldPath, ".%s_%s", indexType, getMulti(isMulti));
        }
    }

    private char getMulti(boolean multi) {
        if (multi) {
            return 'm';
        }
        return 's';
    }

    @Override
    public boolean init(final JsonNode node) {
        is_18n = Is18N.apply(node);
        isId = IsId.apply(node);
        indexType = Name.apply(node);
        return true;
    }
}
