package ht.util.json.iterators;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.json.JSONElement;
import ht.util.json.JSONList;

/**
 * Mapper used in "nesting" an iterator from another iterator of json element.
 * <p/>
 * Could be used outside the nesting iterator.  Great for getting a set of json elements from one blob.
 * <p/>
 * You provide the root path to the JSONList element you want to iterate over.
 */
public class GetJSONIteratorFromPath extends BaseMapper<JSONElement, AbstractIterator<JSONElement>> {
    private String path;

    public GetJSONIteratorFromPath(String path) {
        this.path = path;
    }

    @Override
    public AbstractIterator<JSONElement> apply(final JSONElement e) {
        JSONElement e2 = e.getFromPath(path);
        if (e2 instanceof JSONList) {
            // just give back a one element iterator (its probably an error)
            return new CollectionIterator(((JSONList) e2).get());

        }
        return null;
    }
}