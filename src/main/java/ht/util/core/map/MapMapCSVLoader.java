package ht.util.core.map;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.tools.BaseFileUtil;
import ht.util.core.ArrayUtil;
import ht.util.io.csv.CSVIterator;
import ht.util.io.csv.ColumnTableMeta;

/**
 * Construct a HashHashMap from a csvfile
 */
public abstract class MapMapCSVLoader<L1TYPE extends Object, L2TYPE extends Object, PAYLOAD extends Object> {
    public static final String l1Key = "key1";

    public static final String l2Key = "key2";

    public static final String payloadKey = "payload";

    public HashHashMap<L1TYPE, L2TYPE, PAYLOAD> get(BaseFile bf, HashHashMap<L1TYPE, L2TYPE, PAYLOAD> map, boolean makeSemetric) {
        if (map == null) {
            map = new HashHashMap<L1TYPE, L2TYPE, PAYLOAD>(null);
        }
        CSVIterator iter = BaseFileUtil.bf2csv.apply(bf);
        ColumnTableMeta ctm = ColumnTableMeta.init(iter.getColumnNames());
        while (iter.hasNext()) {
            String row[] = iter.next();

            String pV = ctm.get(payloadKey, row);
            PAYLOAD payload = getPayload(pV);
            String k1 = ctm.get(l1Key, row);
            String k2 = ctm.get(l2Key, row);

            addIt(map, payload, k1, k2);
            if (makeSemetric) {
                addIt(map, payload, k2, k1);
            }
        }
        return map;
    }

    private void addIt(final HashHashMap<L1TYPE, L2TYPE, PAYLOAD> map, final PAYLOAD payload, final String k1, final String k2) {
        L1TYPE l1t = getL1Key(k1);
        L2TYPE l2t = getL2Key(k2);

        if (ArrayUtil.allElementsNotNull(l1t, l2t, payload)) {
            map.put(l1t, l2t, payload);
        }
    }

    public abstract L1TYPE getL1Key(String key);

    public abstract L2TYPE getL2Key(String key);

    public abstract PAYLOAD getPayload(String key);
}
