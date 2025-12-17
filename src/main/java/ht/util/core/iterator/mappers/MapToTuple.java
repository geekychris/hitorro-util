package ht.util.core.iterator.mappers;

import ht.util.core.GenericKeyValue;

import java.util.function.Function;

public class MapToTuple<E, F> implements Function<E, GenericKeyValue<E, F>> {

    private Function<E, F> mapper;

    public MapToTuple(Function<E, F> mapper) {
        this.mapper = mapper;
    }

    @Override
    public GenericKeyValue<E, F> apply(E elem) {
        return new GenericKeyValue<>(elem, mapper.apply(elem));
    }
}
