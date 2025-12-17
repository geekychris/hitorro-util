package ht.util.json.keys;

import ht.util.json.keys.mappers.ResolvableJson2StringMapper;
import ht.util.json.keys.propaccess.Propaccess;

public class ResolvableStringProperty extends StringProperty {
    public ResolvableStringProperty(String path, String description, String defaultValue) {
        super(new Propaccess(path), description, defaultValue);
        mapper = ResolvableJson2StringMapper.instance;
    }

    public ResolvableStringProperty(Propaccess path, String description, String defaultValue) {
        super(path, description, defaultValue);
        mapper = ResolvableJson2StringMapper.instance;
    }
}

