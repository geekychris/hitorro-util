package ht.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.Propaccess;

public class IsoLanguageSeeker implements IndexSeeker {
    public static StringProperty langKey = new StringProperty("lang", "", null);

    public int getIndex(ArrayNode node, Propaccess access, int depth, String value, JVS jvs) {
        if (node == null) {
            return -1;
        }
        for (int i = 0; i < node.size(); i++) {
            String v = langKey.apply(node.get(i));
            if (value.equals(v)) {
                return i;
            }
        }
        if (value.length() > 2) {
            value = value.substring(0, 2);
        }
        for (int i = 0; i < node.size(); i++) {
            String v = langKey.apply(node.get(i));
            if (value.equals(v)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean init(final JsonNode node) {
        return false;
    }
}
