package ht.util.core.iterator.reducers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.BaseReducer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MapReducer<K, E> implements BaseReducer<E, Map<K, E>> {
    private Function<E, K> keyMapper;

    public MapReducer(Function<E, K> keyMapperIn) {
        keyMapper = keyMapperIn;
    }

    public Map<K, E> reduce(AbstractIterator<E> iter) {
        Map<K, E> map = new HashMap();
        while (iter.hasNext()) {
            E e = iter.next();
            K k = keyMapper.apply(e);
            map.put(k, e);
        }
        return map;
    }
}
