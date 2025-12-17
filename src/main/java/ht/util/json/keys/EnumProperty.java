package ht.util.json.keys;

import ht.util.core.EnumContext;
import ht.util.json.keys.mappers.EnumPropertyMapper;
import ht.util.json.keys.propaccess.Propaccess;

public class EnumProperty<E extends Enum> extends BaseMappingProperty<E> {
    public EnumProperty(String path, String description, E defaultValue, EnumContext<E> ec) {
        super(new Propaccess(path), description, defaultValue, new EnumPropertyMapper(ec));
    }

    public EnumProperty(Propaccess path, String description, E defaultValue, EnumContext<E> ec) {
        super(path, description, defaultValue, new EnumPropertyMapper(ec));
    }
}

