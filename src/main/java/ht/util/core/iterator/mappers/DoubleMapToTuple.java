package ht.util.core.iterator.mappers;

import ht.util.core.GenericKeyValue;

public class DoubleMapToTuple<E, F, G> implements BaseFunction<E, GenericKeyValue<F, G>> {

    private BaseFunction<E, F> mapperF;
    private BaseFunction<E, G> mapperG;

    public DoubleMapToTuple(BaseFunction<E, F> mapperF, BaseFunction<E, G> mapperG) {
        this.mapperF = mapperF;
        this.mapperG = mapperG;
    }

    @Override
    public GenericKeyValue<F, G> apply(E elem) {
        return new GenericKeyValue<>(mapperF.apply(elem), mapperG.apply(elem));
    }
}
