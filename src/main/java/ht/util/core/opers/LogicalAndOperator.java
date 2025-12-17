package ht.util.core.opers;

import ht.util.typesystem.annotation.TypeClassMetaInfo;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@TypeClassMetaInfo(shortTypeName = "AndConstraint",
        isView = false,
        isPersisted = false,
        schemaVersion = LogicalAndOperator.SerializationVersion)
public class LogicalAndOperator<T> extends LogicalOperatorCollection<T> {

    public LogicalAndOperator() {

    }

    public LogicalAndOperator(HTPredicate<? super T> p1, HTPredicate<? super T> p2) {
        super(p1, p2);
    }

    public LogicalAndOperator(HTPredicate<? super T>... constraints) {
        super(constraints);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AND(");
        boolean comma = false;
        for (HTPredicate constraint : m_constraints) {
            if (comma == true) {
                builder.append(",");
            } else {
                comma = true;
            }
            builder.append(constraint.toString());

        }
        builder.append(")");
        return builder.toString();
    }

    public boolean test(T field) {
        for (HTPredicate constraint : m_constraints) {
            if (!constraint.test(field)) {
                return false;
            }
        }
        return true;
    }
}
