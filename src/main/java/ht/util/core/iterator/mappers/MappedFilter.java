package ht.util.core.iterator.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.opers.HTPredicate;

/**
 * A filter that maps from some input type via a mapper and then applies the real filter.
 */
public class MappedFilter<E, F> implements HTPredicate<E> {
    private BaseMapper<E, F> mapper;
    private HTPredicate<F> lo;

    public MappedFilter(BaseMapper mapper, HTPredicate<F> lo) {
        this.mapper = mapper;
        this.lo = lo;
    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final E e) {
        F f = mapper.apply(e);
        if (f != null) {
            return lo.test(f);
        }
        return false;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }
}