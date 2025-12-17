package ht.util.core.opers;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public abstract class IntFieldMatch<T> implements HTPredicate<T> {
    private Operator op;
    private int value;
    public IntFieldMatch(Operator op, int v) {
        this.op = op;
        value = v;
    }

    public abstract int getValue(T t);

    public void setValue(int val) {
        value = val;
    }

    public boolean test(T o) {
        int v = getValue(o);

        switch (op) {
            case Equals:
                return v == value;
            case GreaterThan:
                return v > value;
            case LessThan:
                return v < value;
            case GreaterThanOrEqual:
                return v >= value;
            case LessThanOrEqual:
                return v <= value;
        }
        return false;
    }

    public enum Operator {
        Equals, GreaterThan, LessThan, GreaterThanOrEqual, LessThanOrEqual
    }
}