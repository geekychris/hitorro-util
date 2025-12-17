package ht.util.core.map;

import java.io.Serializable;
import java.util.*;

/**
 * The MapFactory is a mechanism for specifying what kind of apply is to be used
 * by some object.  For example, if you want a Counter which is backed by an
 * IdentityHashMap instead of the defaul HashMap, you can pass in an
 * IdentityHashMapFactory.
 *
 * @author Dan Klein
 */

public abstract class MapFactory<K, V> implements Serializable {

    public abstract Map<K, V> buildMap();

    public static class HashMapFactory<K, V> extends MapFactory<K, V> {
        public Map<K, V> buildMap() {
            return new HashMap<K, V>();
        }
    }

    public static class IdentityHashMapFactory<K, V> extends MapFactory<K, V> {
        public Map<K, V> buildMap() {
            return new IdentityHashMap<K, V>();
        }
    }

    public static class TreeMapFactory<K, V> extends MapFactory<K, V> {
        public Map<K, V> buildMap() {
            return new TreeMap<K, V>();
        }
    }

    public static class WeakHashMapFactory<K, V> extends MapFactory<K, V> {
        public Map<K, V> buildMap() {
            return new WeakHashMap<K, V>();
        }
    }
}

