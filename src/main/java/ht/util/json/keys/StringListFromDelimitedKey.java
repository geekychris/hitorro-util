package ht.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.util.json.keys.mappers.StringListMapper;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.List;

public class StringListFromDelimitedKey extends BaseMappingProperty<List<String>> {
    public StringListFromDelimitedKey(String path, String description, String seperator, List<String> defaultValue) {
        super(new Propaccess(path), description, defaultValue, new StringListMapper(seperator));
    }

    public StringListFromDelimitedKey(Propaccess path, String description, String seperator, List<String> defaultValue) {
        super(path, description, defaultValue, new StringListMapper(seperator));
    }

    public String[] getArray(JsonNode node) {
        return getArray(apply(node));
    }

    public String[] getArray(JVS node) {
        return getArray(apply(node));
    }

    public String[] getArray() {
        return getArray(apply());
    }

    private String[] getArray(final List<String> l) {
        if (l == null) {
            return null;
        }
        return l.toArray(new String[l.size()]);
    }
}

