package ht.util.core.iterator.mappers;

import ht.util.core.GenericKeyValue;

import java.util.function.Function;

public interface BaseFunction<E, F> extends Function<E, F> {
    default <G> BaseFunction<E, GenericKeyValue<F, G>> toTuple(BaseFunction<E, G> valueMapper) {
        return new DoubleMapToTuple(this, valueMapper);
    }
}
