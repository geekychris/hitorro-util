package ht.util.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Keep track of objects and a numeric id.
 */
public class IDObjectMap<E> {
    private int counter = 0;
    private TObjectIntHashMap<E> oIntMap = new TObjectIntHashMap<E>();
    private TIntObjectHashMap<E> intOMap = new TIntObjectHashMap<E>();
    private TObjectIntHashMap<String> nameIdMap = new TObjectIntHashMap<String>();

    public final int getOrAdd(E e, String name) {
        if (oIntMap.contains(e)) {
            return oIntMap.get(e);
        }
        int i = counter++;
        oIntMap.put(e, i);
        intOMap.put(i, e);
        nameIdMap.put(name, i);
        return i;
    }

    public final E getObject(int id) {
        return intOMap.get(id);
    }

    public JsonNode getMap() {
        ObjectNode s2i = JsonNodeFactory.instance.objectNode();

        ObjectNode i2s = JsonNodeFactory.instance.objectNode();
        TObjectIntIterator<String> iter = nameIdMap.iterator();
        while (iter.hasNext()) {
            iter.advance();
            String key = iter.key();
            int val = iter.value();
            ObjectNode s2iRow = JsonNodeFactory.instance.objectNode();
            s2iRow.put(key, val);

            i2s.put(Integer.toString(val), key);

        }
        ObjectNode ret = JsonNodeFactory.instance.objectNode();
        ret.set("s2i", s2i);
        ret.set("i2s", i2s);
        return ret;
    }

    public final int getObjectIdByName(String name) {
        if (!nameIdMap.contains(name)) {
            return -1;
        }
        return nameIdMap.get(name);
    }

}
