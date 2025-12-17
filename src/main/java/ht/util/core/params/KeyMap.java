package ht.util.core.params;

import ht.util.core.GenericKeyValue;

import java.util.TreeMap;

/**
 *
 */
public class KeyMap extends GenericKeyValue<String, TreeMap<String, String>> {
    public KeyMap(final String s, final TreeMap<String, String> treeMap) {
        super(s, treeMap);
    }
}
