package ht.util.json.mapper;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.ArrayIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.JSONElement;
import ht.util.json.JSONType;
import ht.util.json.iterators.JSONIterFromJSONList;

/**
 *
 */
public class JSONIterIterMapper extends BaseMapper<JSONElement, AbstractIterator<JSONElement>> {
    public static final JSONIterIterMapper jsonIterIterMapper = new JSONIterIterMapper();

    @Override
    public AbstractIterator<JSONElement> apply(final JSONElement e) {
        if (e.getJSONType() == JSONType.List) {
            return new JSONIterFromJSONList(e);
        } else {
            // just give back a one element iterator (its probably an error)
            return new ArrayIterator(new JSONElement[]{e});
        }
    }
}
