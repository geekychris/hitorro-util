package ht.util.core.longword;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.core.CommandArgs;
import ht.util.json.keys.propaccess.PropaccessError;

import java.text.ParseException;
import java.util.*;

/**
 * Container of all the bits that make up a word (long word in this case)
 */
public class WordBits implements Iterable<NamedBitsOfLong> {
    private Map<String, NamedBitsOfLong> handlers = new HashMap<String, NamedBitsOfLong>();
    private List<NamedBitsOfLong> bits = new ArrayList<NamedBitsOfLong>();
    private int m_nextFree = 0;


    public WordBits clone() {
        WordBits child = new WordBits();
        child.handlers = new HashMap();
        child.handlers.putAll(handlers);
        child.bits = new ArrayList();
        child.bits.addAll(bits);

        return child;
    }

    public NamedBitsOfLong add(String name, int width, BiDirectionalKeyLongMap mapper) {
        return addAux(name, width, mapper);
    }

    public NamedBitsOfLong add(String name, int width) {
        return addAux(name, width, null);
    }

    private NamedBitsOfLong addAux(final String name, final int width, BiDirectionalKeyLongMap mapper) {
        NamedBitsOfLong nl = new NamedBitsOfLong(this, name, m_nextFree, width, mapper);
        m_nextFree += width;
        handlers.put(name, nl);
        bits.add(nl);
        return nl;
    }

    public NamedBitsOfLong get(String name) {
        return handlers.get(name.toLowerCase());
    }

    /**
     * Generate a string of the form: key1=valuea key2=valueb.... where each key is the named bit and where each value
     * is the value of the named bit.
     *
     * @param builder
     * @param l
     */
    public void dumpLongToString(StringBuilder builder, long l) {
        boolean notFirst = false;
        for (NamedBitsOfLong nl : bits) {
            if (notFirst) {
                builder.append(" ");
            } else {
                notFirst = true;
            }
            nl.dumpKeyValueToStringBuilder(builder, l);
        }
    }

    public long setFromKeyValueString(String s, long l) throws ParseException, PropaccessError {
        JVS map = CommandArgs.getParameters(s, false, true);
        return setFromMap(map, l);
    }

    public long setFromMap(JVS map, long l) {
        Iterator<Map.Entry<String, JsonNode>> iter = map.getJsonNode().fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            NamedBitsOfLong nl = handlers.get(entry.getKey().toLowerCase());
            if (nl != null) {
                String v = entry.getValue().textValue();
                v = v.trim();
                if (nl.hasValueMapper()) {
                    // go through the mapping layer
                    l = nl.set(l, v);
                } else {
                    l = nl.set(l, Long.parseLong(v));
                }
            } else {
                //maybe we should warn?
            }
        }
        return l;
    }

    public Iterator<NamedBitsOfLong> iterator() {
        return bits.iterator();
    }
}
