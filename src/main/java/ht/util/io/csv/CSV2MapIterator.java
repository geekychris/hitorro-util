package ht.util.io.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.string.StringUtil;

/**
 * Convert a csv row into a apply.
 */
public class CSV2MapIterator extends AbstractIterator<JsonNode> {
    private CSVIterator csvIter;
    private String header[];

    public CSV2MapIterator(CSVIterator iter) {
        csvIter = iter;
        header = csvIter.getColumnNames();
    }

    @Override
    public void close() throws Exception {
        csvIter.close();
    }

    @Override
    public boolean hasNext() {
        return csvIter.hasNext();
    }

    @Override
    public JsonNode next() {
        return mapIt();
    }

    @Override
    public void remove() {

    }

    private JsonNode mapIt() {
        if (csvIter.hasNext()) {
            String vals[] = csvIter.next();
            ObjectNode map = JsonNodeFactory.instance.objectNode();
            for (int i = 0; i < Math.min(header.length, vals.length); i++) {
                if (StringUtil.nullOrEmptyString(header[i])) {
                    continue;
                }
                if (vals[i] != null) {
                    map.put(header[i], vals[i]);
                }
            }

            return map;
        }
        return null;
    }
}
