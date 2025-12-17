package ht.util.core.opers;

import ht.util.typesystem.annotation.TypeClassMetaInfo;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */

@TypeClassMetaInfo(shortTypeName = "OrConstraint",
        isView = false,
        isPersisted = false,
        schemaVersion = LogicalOrOperator.SerializationVersion)
public class LogicalOrOperator<T> extends LogicalOperatorCollection<T> {
    public LogicalOrOperator() {

    }

    public LogicalOrOperator(HTPredicate<? super T> p1, HTPredicate<? super T> p2) {
        super(p1, p2);
    }

    public LogicalOrOperator(HTPredicate<? super T>... constraints) {
        m_constraints = constraints;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OR(");
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
            if (constraint.test(field)) {
                return true;
            }
        }
        return false;
    }

}

