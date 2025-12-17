package ht.util.json.keys;

import ht.util.json.keys.mappers.BytesToLong;
import ht.util.json.keys.propaccess.Propaccess;

public class LongFromBytesProperty extends BaseMappingProperty<Long> {
    public LongFromBytesProperty(String path, String description, Long defaultValue) {
        super(new Propaccess(path), description, defaultValue, BytesToLong.instance);
    }

    public LongFromBytesProperty(Propaccess path, String description, Long defaultValue) {
        super(path, description, defaultValue, BytesToLong.instance);
    }
}

