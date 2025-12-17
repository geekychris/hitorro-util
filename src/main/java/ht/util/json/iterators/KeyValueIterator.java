package ht.util.json.iterators;

import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.AbstractIterator;
import ht.util.json.JSONElement;

/**
 * Reads a stream of JSON objects of which one is a key and the other is a value.  We pair these up and put them in a
 * GKV.
 */
public class KeyValueIterator<K, V> extends AbstractIterator<GenericKeyValue<K, V>> {
    private AbstractIterator iter;

    public KeyValueIterator(AbstractIterator<JSONElement> iter) {
        this.iter = iter;
    }

    @Override
    public void close() throws Exception {
        iter.close();
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public GenericKeyValue<K, V> next() {
        K key = (K) iter.next();
        if (iter.hasNext()) {
            V value = (V) iter.next();
            return get(key, value);
        }
        return null;
    }

    protected GenericKeyValue<K, V> get(K key, V value) {
        return new GenericKeyValue<K, V>(key, value);
    }

    @Override
    public void remove() {
    }
}
