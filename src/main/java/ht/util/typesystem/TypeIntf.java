package ht.util.typesystem;

import ht.util.core.opers.HTPredicate;

import java.util.List;

public interface TypeIntf extends TypeBaseIntf {
    TypeFieldIntf[] getTypeFields();

    TypeFieldIntf getField(String fieldName);

    TypeFieldIntf getLanguageField();

    ht.util.typesystem.annotation.TypeClassMetaInfo getTypeMeta();

    String getName();

    TypeIntf getView(String view);

    String getViewName();

    boolean isView();

    boolean isPersisted();

    String getSoftGuid(HTSerializable pt);

    List<TypeFieldIntf> getTypeFieldsByConstraint(HTPredicate<TypeFieldIntf> constraint);
}