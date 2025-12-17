package ht.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 12, 2006 Time: 4:50:31 PM
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

