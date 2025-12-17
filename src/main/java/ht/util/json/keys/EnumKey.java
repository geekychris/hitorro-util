package ht.util.json.keys;

import ht.util.json.keys.mappers.EnumKeysMapper;
import ht.util.json.keys.propaccess.Propaccess;

public class EnumKey<E extends Enum> extends BaseMappingProperty<E> {
    public EnumKey(final String access, final String description, final E values[], final E defaultValue) throws PropertyException {
        super(new Propaccess(access), description, defaultValue, new EnumKeysMapper(values));
    }

    public EnumKey(final Propaccess access, final String description, final E values[], final E defaultValue) throws PropertyException {
        super(access, description, defaultValue, new EnumKeysMapper(values));
    }
}


