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
package ht.util.typesystem;

import ht.util.typesystem.valuesource.ValueMapMapper;

public interface TypeFieldIntf extends FieldBaseIntf {
    Class getImplementingClass();

    Class getReturnType();

    ValueMapMapper getValueMapMapper();

    void setValueMapMapper(ValueMapMapper mapper);

    String getName();

    Object getValue(Object obj);

    void setValue(Object obj, Object value);

    ht.util.typesystem.annotation.FullTextAttributeMetaInfo getFullTextMeta();

    void setFullTextMeta(ht.util.typesystem.annotation.FullTextAttributeMetaInfo meta);

    ht.util.typesystem.annotation.UiProperties getUiProperties();

    void setUiProperties(ht.util.typesystem.annotation.UiProperties prop);

    ht.util.typesystem.annotation.DBSearchableAttributeMetaInfo getDbSearchableMeta();

    void setDbSearchableMeta(ht.util.typesystem.annotation.DBSearchableAttributeMetaInfo meta);

    TypeIntf getDefinedIn();
}
