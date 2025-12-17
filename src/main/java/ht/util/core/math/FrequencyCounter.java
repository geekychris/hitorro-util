package ht.util.core.math;


import gnu.trove.map.hash.TIntIntHashMap;

/**
 * Count the frequency.
 */
public class FrequencyCounter {
    private TIntIntHashMap map = new TIntIntHashMap();

    public void increment(int position) {
        if (map.contains(position)) {
            map.increment(position);
        } else {
            map.put(position, 1);
        }
    }

    public void increment(String pos) {
        increment(Integer.parseInt(pos));
    }
}
