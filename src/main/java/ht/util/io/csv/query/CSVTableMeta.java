package ht.util.io.csv.query;


import ht.util.core.UtilDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CSVTableMeta {
    private List<CSVTableMetaItem> cols = new ArrayList();
    private Map<String, CSVTableMetaItem> map = new HashMap();

    public void add(String field, UtilDataType dt) {
        field = field.toLowerCase();
        CSVTableMetaItem item = new CSVTableMetaItem();
        item.setField(field);
        item.setDt(dt);
        cols.add(item);
        map.put(field, item);
    }

    public int getSize() {
        return cols.size();
    }

    public void addAll(String fields[], UtilDataType dts[]) {
        for (int i = 0; i < fields.length; i++) {
            add(fields[i], dts[i]);
        }
    }

    public CSVTableMetaItem get(String field) {
        return map.get(field.toLowerCase());
    }

    public CSVTableMetaItem get(int i) {
        return cols.get(i);
    }
}
