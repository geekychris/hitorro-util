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
