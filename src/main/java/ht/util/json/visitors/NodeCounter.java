package ht.util.json.visitors;

import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import ht.util.core.Console;
import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.JsonValueSource;
import ht.util.core.string.StringUtil;

import java.util.*;

/**
 * Count the occurrences of paths within json tree and also record the first instance of a node so that one can reverse
 * engineer a type from it.
 */
public class NodeCounter implements JSONVisitor {
    public TObjectIntHashMap<String> pathToCount = new TObjectIntHashMap();
    public HashMap<String, JsonNode> elementType = new HashMap();
    List<String> path = new ArrayList();
    StringBuilder b = new StringBuilder();

    public NodeCounter() {
    }

    @Override
    public void visit(final JsonValueSource elem, String name, final int depth) {

    }

    private void inc(JsonNode elem) {
        b.setLength(0);
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) {
                b.append(".");
            }
            b.append(path.get(i));
        }
        String key = b.toString();
        if (pathToCount.containsKey(key)) {
            pathToCount.increment(key);
        } else {
            pathToCount.put(key, 1);
        }

        if (!this.elementType.containsKey(key)) {
            if (elem.isNull()) {
                elementType.put(key, elem);
            }
        }
    }

    @Override
    public void visit(final JsonValueSource elem, final int depth) {
        visit(elem.getNode(), depth);
    }

    private void visit(final JsonNode elem, final int depth) {
        if (elem.isTextual() || elem.isBoolean() || elem.isNumber()) {
            inc(elem);
        } else if (elem.isObject()) {
            Iterator<String> iter = elem.fieldNames();
            while (iter.hasNext()) {
                String key = iter.next();
                JsonNode v = elem.get(key);
                path.add(key);
                visit(v, depth + 1);
                path.remove(path.size() - 1);
            }
        }

    }

    public List<GenericKeyValue<String, Integer>> getDumpOfCounts() {
        List<GenericKeyValue<String, Integer>> list = new ArrayList();

        for (TObjectIntIterator<String> iter = pathToCount.iterator(); iter.hasNext(); ) {
            iter.advance();
            String key = iter.key();
            int val = iter.value();
            list.add(new GenericKeyValue(key, new Integer(val)));
        }
        return list;
    }

    public List<GenericKeyValue<String, JsonNode>> getDumpOfTypes() {
        List<GenericKeyValue<String, JsonNode>> list = new ArrayList();
        Set<Map.Entry<String, JsonNode>> set = elementType.entrySet();
        Iterator<Map.Entry<String, JsonNode>> iter = set.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> ent = iter.next();
            String key = ent.getKey();
            JsonNode elem = ent.getValue();
            list.add(new GenericKeyValue(key, elem));
        }
        return list;
    }

    public void printElemsAsType() {
        List<GenericKeyValue<String, JsonNode>> list = getDumpOfTypes();
        for (GenericKeyValue<String, JsonNode> e : list) {
            Class type = null;
            JsonNode elem = e.getValue();
            String annotation = "";
            if (elem.isTextual()) {
                type = String.class;
            } else if (elem.isBoolean()) {
                type = Boolean.class;
            } else if (elem.isNumber()) {
                Number n = elem.numberValue();
                annotation = n.toString();
                type = Long.class;
            }

            if (type != null) {
                Console.println("<%s class=\"%s\">", StringUtil.replace(e.getKey(), ".", "---"), type.getCanonicalName());
                Console.println("<!-- %s -->", annotation);
                Console.println("</%s>", e.getKey());
            }

        }

    }

}
