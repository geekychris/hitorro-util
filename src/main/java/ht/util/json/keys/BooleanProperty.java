package ht.util.json.keys;


import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.json.keys.mappers.JsonNodeToBoolean;
import ht.util.json.keys.propaccess.Propaccess;

/**
 *
 */
public class BooleanProperty extends BaseMappingProperty<Boolean> {
    public BooleanProperty(DebugArgAno ano) {
        this(ano.keyName(), ano.description(), Boolean.parseBoolean(ano.defaultValue()));
    }

    public BooleanProperty(String path, String description, Boolean defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToBoolean.instance);
    }

    public BooleanProperty(Propaccess path, String description, Boolean defaultValue) {
        super(path, description, defaultValue, JsonNodeToBoolean.instance);
    }
}

