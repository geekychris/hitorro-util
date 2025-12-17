package ht.util.io.csv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.string.StringUtil;

public class CSV2JsonNodeIterator extends AbstractIterator<JsonNode> {
    private CSVIterator csvIter;
    private String header[];

    public CSV2JsonNodeIterator(CSVIterator iter) {
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
        return map2Json();
    }

    @Override
    public void remove() {

    }

    private JsonNode map2Json() {
        if (csvIter.hasNext()) {
            String vals[] = csvIter.next();

            ObjectNode jMap = JsonNodeFactory.instance.objectNode();

            for (int i = 0; i < Math.min(header.length, vals.length); i++) {
                if (StringUtil.nullOrEmptyString(header[i])) {
                    continue;
                }
                jMap.put(header[i], vals[i]);
            }
            return jMap;
        }
        return null;
    }
}
