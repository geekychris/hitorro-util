package ht.util.core.iterator;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.GenericKeyValue;
import ht.util.json.JsonInitable;

import java.util.function.Function;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public interface Mapper<I, O> extends Function<I, O>, JsonInitable {
    default boolean init(JsonNode node) {
        return true;
    }

    default O apply(I i, IsoLanguageIntf lang) {
        return apply(i);
    }

    default Mapper<I, GenericKeyValue<I, O>> tuple() {
        return (I i) -> new GenericKeyValue<I, O>(i, apply(i));
    }

    default <U> Mapper<I, GenericKeyValue<O, U>> tuple(Mapper<I, U> secondMapper) {
        return (I i) -> new GenericKeyValue(apply(i), secondMapper.apply(i));
    }
}
