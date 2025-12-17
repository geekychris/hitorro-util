package ht.util.json.keys;


import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.mappers.JsonNodeToInteger;
import ht.util.json.keys.propaccess.Propaccess;

/**
 *
 */
public class IntegerProperty extends BaseMappingProperty<Integer> {
    public IntegerProperty(DebugArgAno ano) {
        this(ano.keyName(), ano.description(), Integer.parseInt(ano.defaultValue()));
    }

    public IntegerProperty(String path, String description, Integer defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToInteger.instance);
    }

    public IntegerProperty(Propaccess path, String description, Integer defaultValue) {
        super(path, description, defaultValue, JsonNodeToInteger.instance);
    }
}
