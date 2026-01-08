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
package com.hitorro.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FullTextAttributeMetaInfo {
    /**
     * Name that this method is identified by.
     *
     * @return
     */
    String displayName();

    /**
     * Indicates this field is to be full text indexed.
     *
     * @return
     */
    boolean isFullTextIndexable();

    /**
     * List of filters associated with this getter
     *
     * @return
     */
    String luceneIndexingFilters() default "STANDARD,CASE";

    /**
     * List of filters used for analyzing the query string if different than the indexing filters.
     *
     * @return
     */
    String luceneSearchingFilters() default "STANDARD,CASE";

    /**
     * Name of the lucene field that this getter is to be indexed and searched as.
     *
     * @return
     */
    String luceneFieldName();

    /**
     * The field should be stored, that is, used as a key identifier in the lucene "store"
     *
     * @return true if to be stored
     */

    boolean stored() default false;

    /**
     * @return process as a date
     */

    boolean isDate() default false;

    Class indexerClass() default Object.class;
    //Class indexerClass();

    /**
     * String literals are not tokenized
     *
     * @return true if to be stored as a string literal (no analysis is done)
     */
    boolean stringLiteral();

    /**
     * Content should be merged into "all" field ...this is a glob field used to collect the textual output and allow a
     * search such as all:bla rather than title:bla body:bla....
     *
     * @return true if to be indexed in all field
     */
    boolean allField();
}

