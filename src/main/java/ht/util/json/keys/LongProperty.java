package ht.util.json.keys;

import ht.util.json.keys.mappers.JsonNodeToLong;
import ht.util.json.keys.propaccess.Propaccess;

/**
 *
 */
public class LongProperty extends BaseMappingProperty<Long> {
    public LongProperty(String path, String description, Long defaultValue) {
        super(new Propaccess(path), description, defaultValue, JsonNodeToLong.instance);
    }

    public LongProperty(Propaccess path, String description, Long defaultValue) {
        super(path, description, defaultValue, JsonNodeToLong.instance);
    }
}