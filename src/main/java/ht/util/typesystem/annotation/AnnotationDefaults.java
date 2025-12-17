package ht.util.typesystem.annotation;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 13, 2006 Time: 11:00:06 AM Class used to
 * define "default / flyweight" annotations AND to allow us to specify a listFiles
 */
@TypeClassMetaInfo(shortTypeName = "NA",
        isView = false,
        isPersisted = true)
@UiTypeProperties(name = "class?")
public abstract class AnnotationDefaults {
    @DBSearchableAttributeMetaInfo()
    @FullTextAttributeMetaInfo(displayName = "", isFullTextIndexable = false, luceneIndexingFilters = "",
            luceneFieldName = "", stringLiteral = false, allField = false)
    @UiProperties(displayName = "")
    public abstract void annotationDefaults();
}


