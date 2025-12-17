package ht.util.urlparser;

import ht.util.core.events.cache.SingletonCache;
import ht.util.core.iterator.mappers.BaseMapper;

/**
 *
 */
public class TLDDictionarySingleton extends BaseMapper<Object, TLDDictionary> {
    public static final String Eventname = "TLDDictionarySingleton";

    public static final SingletonCache<TLDDictionary> me = new SingletonCache(Eventname, new TLDDictionarySingleton());

    public TLDDictionarySingleton() {
    }

    public TLDDictionary apply(Object o) {
        TLDDictionary dict = new TLDDictionary();

        dict.load();

        return dict;
    }
}
