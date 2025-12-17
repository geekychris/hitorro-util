package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public interface HTPredicate<T> extends Predicate<T> {
    /**
     * Initialized for pass through a scan
     */
    default void initForPass() {

    }

    default boolean initFromMap(JsonNode map) {
        return true;
    }

    default HTPredicate<T> and(HTPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return new LogicalAndOperator<T>(this, other);
    }

    /**
     * Returns a predicate that represents the logical negation of this
     * predicate.
     *
     * @return a predicate that represents the logical negation of this
     * predicate
     */
    default HTPredicate<T> negate() {
        return new LogicalNotOperator(this);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default HTPredicate<T> or(HTPredicate<? super T> other) {
        Objects.requireNonNull(other);
        return new LogicalOrOperator<T>(this, other);
    }
}