package ht.util.core.longword;


import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

/**
 *
 */
public class BiDirectionalKeyLongMap {
    private TObjectLongHashMap<String> map = new TObjectLongHashMap();
    private TLongObjectHashMap<String> reverse = new TLongObjectHashMap();
    private boolean ignoreCase;

    public BiDirectionalKeyLongMap(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String mapToString(final long l) {
        return reverse.get(l);
    }

    public long mapFromString(final String e) {
        Long v = null;
        if (ignoreCase) {
            v = map.get(e.toLowerCase());
        } else {
            v = map.get(e);
        }
        if (v == null) {
            return Long.parseLong(e);
        }
        return v;
    }

    public void add(String key, long val) {
        if (ignoreCase) {
            map.put(key.toLowerCase(), val);
            reverse.put(val, key);
        } else {
            map.put(key, val);
            reverse.put(val, key);
        }
    }
}
