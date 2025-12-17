/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.map;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.io.csv.ColumnTableMeta;

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
