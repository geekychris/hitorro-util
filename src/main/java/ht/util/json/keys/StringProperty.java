package ht.util.json.keys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.mappers.JsonNodeToString;
import ht.util.json.keys.propaccess.Propaccess;

/**
 *
 */
public class StringProperty extends BaseMappingProperty<String> {
    public StringProperty(DebugArgAno ano) {
        this(ano.keyName(), ano.description(), ano.defaultValue());
    }

    public StringProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToString.instance);
    }

    public StringProperty(Propaccess path, String description, String defaultValue) {
        super(path, description, defaultValue, JsonNodeToString.instance);
    }
}
